package com.example.whatsapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

import com.example.whatsapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewTitulo;
    private CircleImageView circleImageViewFoto;
    private Usuario usuarioDetinatario;

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
        textViewTitulo = findViewById(R.id.textViewChatNome);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);


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

        }

    }

}
