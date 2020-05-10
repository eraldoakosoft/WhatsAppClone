package com.example.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.whatsapp.R;
import com.example.whatsapp.activity.ChatActivity;
import com.example.whatsapp.adapter.ConversasAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas;
    private List<Conversa> listConversas = new ArrayList<>();
    private List<Conversa> listConversas2 = new ArrayList<>();
    private ConversasAdapter adapter;
    private ConversasAdapter adap;
    private DatabaseReference database;
    private DatabaseReference conversaRef;
    private ChildEventListener childEventListenerConversas;

    public ConversasFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_conversas, container, false);


        recyclerViewConversas = view.findViewById(R.id.recycleViewConversas);

        database = ConfiguracaoFirebase.getDatabaseReference();
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        conversaRef = database.child("conversas").child( identificadorUsuario );

        //CONFIGURAR ADAPTER
        adapter = new ConversasAdapter(listConversas, getActivity());

        //CONFIGURAR RECYCLERVIEW
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getActivity() );
        recyclerViewConversas.setLayoutManager( layoutManager );
        recyclerViewConversas.setHasFixedSize( true );
        recyclerViewConversas.setAdapter( adapter );


        recyclerViewConversas.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerViewConversas, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Conversa conversaSelecionada = listConversas.get( position );
                        Intent i = new Intent(getActivity(), ChatActivity.class);
                        i.putExtra("chatContato",conversaSelecionada.getUsuarioExibicao());
                        startActivity( i );
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }));
       return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversaRef.removeEventListener(childEventListenerConversas);
    }

    public void recuperarConversas(){
        listConversas.clear();
            childEventListenerConversas = conversaRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    //RECUPERAR CONVERASAS
                    Conversa conversa = dataSnapshot.getValue( Conversa.class );
                    listConversas.add( conversa );
                    adicionarArray(listConversas);
                    adapter.notifyDataSetChanged();

                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    //RECUPERAR CONVERASAS
                    Conversa conversa = dataSnapshot.getValue( Conversa.class );
                    listConversas.add( conversa );
                    adicionarArray(listConversas);
                    adapter.notifyDataSetChanged();

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

    public boolean adicionarArray(List<Conversa> con){
        listConversas2 = con;
        System.out.println("########################");
        System.out.println("Tamanho dentro da função: " + listConversas2.size());
        //adap.notifyDataSetChanged();
        return true;
    }

    public void pesquisarConversas(String texto){
        //Log.d("Enveto ", texto);

        //recuperarConversas();

        System.out.println("+++++++++++++++++++++++++++++");
        System.out.println("Tamanho do Array -----> " + listConversas2.size());


        List<Conversa> listaConversasBusca = new ArrayList<>();

        for ( Conversa conversa : listConversas2  ){

            String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
            String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

            if( nome.contains(texto) || ultimaMsg.contains(texto)){
                listaConversasBusca.add( conversa );
            }
        }

        adap = new ConversasAdapter(listaConversasBusca, getActivity());
        recyclerViewConversas.setAdapter( adap );


    }

}
