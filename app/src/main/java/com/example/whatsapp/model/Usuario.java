package com.example.whatsapp.model;

import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Usuario {
    private String nome;
    private String email;
    private String senha;
    private String idUsuario;

    public Usuario() {
    }

    public void salvar(){
        DatabaseReference databaseReference = ConfiguracaoFirebase.getDatabaseReference();
        DatabaseReference usuario = databaseReference.child("usuarios").child( getIdUsuario() );

        usuario.setValue( this );

    }

    @Exclude
    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
