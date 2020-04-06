package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Permissao;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfigActivity extends AppCompatActivity {

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private ImageButton imageButtonCamera, imageButtonGaleria;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private CircleImageView circleImageView;
    private StorageReference storageReference;
    private String identificadorUsuario;
    private EditText editTextConfigNome;
    private ImageView imageViewConfiAtualizarNome;
    private Usuario usuarioLogado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //CONFIGURAÇÕES INICIAIS
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();

        //VALIDAR PERMISSÕES
        Permissao.validarPermissoes(permissoes, this,1);

        imageViewConfiAtualizarNome = findViewById(R.id.imageViewConfiAtualizarNome);
        editTextConfigNome = findViewById(R.id.editTextConfigNome);
        imageButtonCamera = findViewById(R.id.imageButtonConfigCamera);
        imageButtonGaleria = findViewById(R.id.imageButtonConfigGaleria);
        circleImageView = findViewById(R.id.circleImageFotoPerfil);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");

        //RECUPERAR DADOS DO USUARIO
        FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
        Uri url = user.getPhotoUrl();
        if ( url != null){
            Glide.with(ConfigActivity.this)
                    .load( url )
                    .into( circleImageView );
        }else{
            circleImageView.setImageResource(R.drawable.padrao);
        }

        editTextConfigNome.setText( user.getDisplayName() );

        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent , SELECAO_CAMERA);
                }


            }
        });

        imageViewConfiAtualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = editTextConfigNome.getText().toString();
                boolean retorno = UsuarioFirebase.atualizarNomeUsuario( nome );
                if ( retorno ){
                    usuarioLogado.setNome( nome );
                    usuarioLogado.atualizar();
                    Toast.makeText(ConfigActivity.this, "Nome alterado com sucesso!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageButtonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i , SELECAO_GALERIA);
                }

            }
        });


    }

    private Bitmap abreFotoERotaciona(String caminhoFoto, int angulo) {
        // Abre o bitmap a partir do caminho da foto Bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(caminhoFoto);
        // Prepara a operação de rotação com o ângulo escolhido
        Matrix matrix = new Matrix(); matrix.postRotate(angulo);
        // Cria um novo bitmap a partir do original já com a rotação aplicada
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK ){
            Bitmap imagem = null;

            try {
                switch ( requestCode ){
                    case SELECAO_CAMERA :
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                    case SELECAO_GALERIA :
                        Uri localImagensSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagensSelecionada);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(-90);
                        Bitmap rotated = Bitmap.createBitmap(imagem,0,0,imagem.getWidth(),imagem.getHeight(), matrix, true);
                        imagem = rotated;
                        break;
                }
                if (imagem != null){
                    circleImageView.setImageBitmap( imagem );

                    //RECUPERAR DADOS DA IMAGEM PARA O FIREBASE
                    ByteArrayOutputStream baos =  new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //SALVAR IMAGEM NO FIREBASE
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("perfil")
                            .child( identificadorUsuario )
                            .child("perfil.jpeg");
                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfigActivity.this,
                                    "Erro ao fazer upload da imagem!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> firebaseUrl = taskSnapshot.getStorage().getDownloadUrl();
                            while ( !firebaseUrl.isComplete() );
                            Uri firebase = firebaseUrl.getResult();

                            Toast.makeText(ConfigActivity.this,
                                    "Sucesso ao fazer upload da imagem!",
                                    Toast.LENGTH_SHORT).show();
                            atualizarFotoUsuario( firebase );
                        }
                    });
                }

            }catch ( Exception e ){
                e.printStackTrace();
            }
        }
    }

    public void atualizarFotoUsuario(Uri url){
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
        if ( retorno ){
            usuarioLogado.setFoto( url.toString());
            usuarioLogado.atualizar();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for ( int permissaoResultado : grantResults ){
            if ( permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões!");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
