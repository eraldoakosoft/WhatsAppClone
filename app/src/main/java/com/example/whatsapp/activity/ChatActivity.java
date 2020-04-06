package com.example.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp.adapter.MensagensAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Mensagem;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private EditText editMsg;
    private MensagensAdapter mensagensAdapter;
    private List<Mensagem> listaMensagens = new ArrayList<>();
    private DatabaseReference mensagensRef;
    private StorageReference storageReference;
    private ChildEventListener childEventListenerMensagens;
    private Usuario usuarioDetinatario;
    private static final int SELECAO_CAMERA = 100;

    //IDENTIFICADOR USUARIOS REMETENTE E DESTINATARIO
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //CONFIGURAR TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //CONFIGURACOES INICIAIS
        TextView textViewTitulo = findViewById(R.id.textViewChatNome);
        CircleImageView circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMsg = findViewById(R.id.editTextChatMensagem);
        RecyclerView recyclerViewMensagens = findViewById(R.id.recycleMensagens);
        ImageView imageViewChatImagem = findViewById(R.id.imageViewChatMensagem);

        //RECUPERAR DADOS USUARIO REMETENTE
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();

        //RECUPERAR DADOS DO USUARIO DESTINATARIO
        Bundle bundle = getIntent().getExtras();
        if( bundle != null ){

            usuarioDetinatario = (Usuario) bundle.getSerializable("chatContato");
            textViewTitulo.setText(usuarioDetinatario.getNome());
            String foto = usuarioDetinatario.getFoto();
            if ( foto != null ){
                Uri url = Uri.parse(foto);
                Glide.with(ChatActivity.this)
                        .load(url)
                        .into(circleImageViewFoto);
            }else {
                circleImageViewFoto.setImageResource(R.drawable.padrao);
            }

            //RECUPERAR DADOS USUARIO DESTINATARIO
            idUsuarioDestinatario = Base64Custom.codificarBase64( usuarioDetinatario.getEmail() );

        }


        //CONFIGURAÇÃO ADAPER
        mensagensAdapter = new MensagensAdapter(listaMensagens, getApplicationContext());

        //CONFIGURAÇÃO RECYCLERVIEW
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewMensagens.setLayoutManager( layoutManager );
        recyclerViewMensagens.setHasFixedSize( true );
        recyclerViewMensagens.setAdapter(mensagensAdapter);

        DatabaseReference database = ConfiguracaoFirebase.getDatabaseReference();
        mensagensRef = database.child("mensagens").child( idUsuarioRemetente ).child( idUsuarioDestinatario );


        //EVENTO DE CLIQUE CAMERA
        imageViewChatImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, SELECAO_CAMERA );
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ( resultCode == RESULT_OK ){
            Bitmap bitmap = null;
            try{
                switch ( requestCode ){
                    case SELECAO_CAMERA:
                        bitmap = (Bitmap) data.getExtras().get("data");
                        break;
                }

                if (bitmap != null){
                    //RECUPERAR DADOS DA IMAGEM PARA O FIREBASE
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,80, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //CRIA NOME DA IMAGEM UUID CRIA UMA CHAVE COM BASE NA DATA HORA MINUTOS E SEGUNDO E CRIPTOGRAFA
                    String nomeImgagem = UUID.randomUUID().toString();

                    //CONFIGURAR REFERENCIA DO FIREBASE
                    StorageReference imagem = storageReference.child("imagens")
                            .child("foto")
                            .child( idUsuarioRemetente )
                            .child(nomeImgagem);

                    UploadTask uploadTask = imagem.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Erro", "Erro ao fazer upload da imagem no chat");
                            Toast.makeText(ChatActivity.this, "Erro ao fazer upload!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> firebaseUrl = taskSnapshot.getStorage().getDownloadUrl();
                            while ( !firebaseUrl.isComplete() );
                            Uri uri = firebaseUrl.getResult();
                            String url = uri.toString();

                            Mensagem mensagem = new Mensagem();
                            mensagem.setMensagem("");
                            mensagem.setIdUsuario( idUsuarioRemetente );
                            mensagem.setFoto( url );

                            //SALVAR MENSAGEM REMETENTE
                            salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario,mensagem);
                            //SALVAR MENSAGEM REMETENTE
                            salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);
                            Toast.makeText(ChatActivity.this, "Sucesso ao enviar imagem!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }catch ( Exception e){
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void enviarMensagem(View view){
        String textoMensagem = editMsg.getText().toString();
        if ( !textoMensagem.isEmpty() ){

            Mensagem mensagem = new Mensagem();
            mensagem.setIdUsuario( idUsuarioRemetente );
            mensagem.setMensagem( textoMensagem );

            //SALVAR MENSAGEM PARA O REMETENTE
            salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);
            //SALVAR MENSAGEM PARA O DESTINATARIO
            salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);
            //SALVAR CONVERSA
            salvarConversa(mensagem);

        }else{
            Toast.makeText(ChatActivity.this,
                    "Digite uma mensagem para enviar!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg){
        DatabaseReference database = ConfiguracaoFirebase.getDatabaseReference();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente).child(idDestinatario).push().setValue(msg);

        //LIMPAR EDITTEXT
        editMsg.setText("");
    }

    private void salvarConversa(Mensagem msg){
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente( idUsuarioRemetente );
        conversaRemetente.setIdDestinatario( idUsuarioDestinatario );
        conversaRemetente.setUltimaMensagem( msg.getMensagem() );
        conversaRemetente.setUsuarioExibicao( usuarioDetinatario );
        conversaRemetente.salvar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    private void recuperarMensagens(){

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Mensagem mensagem = dataSnapshot.getValue( Mensagem.class );
                listaMensagens.add( mensagem );
                mensagensAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
