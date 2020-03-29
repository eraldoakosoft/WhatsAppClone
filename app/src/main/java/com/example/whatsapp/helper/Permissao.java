package com.example.whatsapp.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requesteCode){
        if(Build.VERSION.SDK_INT >= 23){
            List<String> listaPermissoes = new ArrayList<>();

            /*PERCORRE AS PERMISSÕES PASSADAS,
            * VERIFICANDO UMA A UMA
            * SE JÁ TEM PERMISSÃO LIBERADA*/
            for( String permissao : permissoes ){
               Boolean tempermissao =  ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;
               if ( !tempermissao ) listaPermissoes.add(permissao);
            }

            /*CASO A LISTA STEJA VAZIA, NÃO É NECESSÁRIO SOLICITAR PERMISSÃO*/
            if ( listaPermissoes.isEmpty() ) return true;
            String[] novasPermissoes = new String[ listaPermissoes.size() ];
            listaPermissoes.toArray( novasPermissoes );
            //SOLICITAR PERMISSÃO
            ActivityCompat.requestPermissions(activity, novasPermissoes, requesteCode);

        }

        return true;
    }

}
