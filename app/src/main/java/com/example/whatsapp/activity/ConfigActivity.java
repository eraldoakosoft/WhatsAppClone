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
import android.widget.ImageButton;

import com.example.whatsapp.R;
import com.example.whatsapp.helper.Permissao;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);


        //VALIDAR PERMISSÕES
        Permissao.validarPermissoes(permissoes, this,1);

        imageButtonCamera = findViewById(R.id.imageButtonConfigCamera);
        imageButtonGaleria = findViewById(R.id.imageButtonConfigGaleria);
        circleImageView = findViewById(R.id.circleImageFotoPerfil);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");


        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent , SELECAO_CAMERA);
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
                }

            }catch ( Exception e ){
                e.printStackTrace();
            }
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
