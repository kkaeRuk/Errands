package com.dasong.errands;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dasong.errands.adapter.List_Adapter;
import com.dasong.errands.model.List_Item;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class List_Activity extends Activity{
    private Activity activity;
    public static Context context;
    private ArrayList<List_Item> m_arr;
    private List_Adapter adapter;

    SwipeRefreshLayout swipeLayout;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ListView list;
    Button btn;

    //private ServiceApi service;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Getting SwipeContainerLayout
        // 당겨서 새로고침 코드
        swipeLayout = findViewById(R.id.swipe_container);
        // Adding Listener
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code here
                setList();
                Toast.makeText(getApplicationContext(), "새로고침", Toast.LENGTH_LONG).show();
                // To keep animation for 4 seconds
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        // Stop animation (This will be after 3 seconds)
                        swipeLayout.setRefreshing(false);
                    }
                }, 3000); // Delay in millis
            }
        });

        FloatingActionButton write = (FloatingActionButton) findViewById(R.id.btn_write);
        //플로팅 버튼 클릭 리스너
        write.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(List_Activity.this, List_Write.class);
                startActivity(intent);
            }
        });

        init();
    }

    //listView 초기화
    public void init(){
        list=(ListView)findViewById(R.id.listView);
        setList();
    }

    //DB에 있는 모든 게시물들 받아오기
    private void setList(){
        m_arr = new ArrayList<List_Item>();
        ListView lv = (ListView)findViewById(R.id.listView);

        //m_arr에 DB에 있는 게시물들 넣기
        db.collection("board")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        m_arr = new ArrayList<List_Item>();
                        ListView lv = (ListView)findViewById(R.id.listView);
                        if (task.isSuccessful()) {
                            int table=1;
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Log.d(TAG, document.getId() + " => " + document.getData());
                                m_arr.add(new List_Item(document.getId(),document.getString("ttitle"),
                                        document.getString("tname"), document.getLong("tdate"),
                                        document.getString("tstart"), document.getString("tarrive"),
                                        document.getString("tcontent"), document.getString("tprice"),
                                        document.getString("count")));
                                System.out.println(m_arr);

                            }

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                        Collections.sort(m_arr, new Comparator<List_Item>() {
                            @Override
                            public int compare(List_Item l1, List_Item l2) {
                                if (l1.getDate() < l2.getDate()) {
                                    return 1;
                                } else if (l1.getDate() > l2.getDate()) {
                                    return -1;
                                }
                                return 0;
                            }
                        });

                        // List_Adapter로 레이아웃에 m_arr에 있는 데이터 넣기
                        adapter = new List_Adapter(List_Activity.this, m_arr);
                        lv.setAdapter(adapter);
                        lv.setDivider(null);
                        lv.setDividerHeight(10);// 구분선의 굵기를 좀 더 크게 하고싶으면 숫자로 높이 지정가능.
                        // Add a new document with a generated ID
                    }
                });
    }

    public void listUpdate(){
        this.setList();
    }
}
