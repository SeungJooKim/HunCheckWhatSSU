package ssu.ssu.huncheckwhatssu;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import ssu.ssu.huncheckwhatssu.utilClass.Book;
import ssu.ssu.huncheckwhatssu.utilClass.Customer;
import ssu.ssu.huncheckwhatssu.utilClass.Trade;

public class FirebaseCommunicator {
    enum WhichRecyclerView {
        searchRecyclerView, sellRecyclerView, ongoingRecyclerView, doneRecyclerView, none
    }

    //Firebase 로그인 계정
    private static FirebaseUser user = null;
    //계정의 이름_UID로 이루어진 string
    private static String userPath = null;
    //DB root/userPath 의 Reference
    private static DatabaseReference myRef = null;
    private static DatabaseReference tradeRef = null;
    private static Vector tradeListVector = null;
    private static Vector sellListVector = null;
    private static Vector buyListVector = null;
    //RecyclerView 설정
    private RecyclerView recyclerView;
    private Context context;
    private Activity activity;
    private WhichRecyclerView whichRecyclerView;

    ValueEventListener tradeEventListener;

    //  FirebaseCommunicator 생성자, 초기화 실행
    public FirebaseCommunicator(WhichRecyclerView whichRecyclerView) {
        this.whichRecyclerView = whichRecyclerView;
        //  user 값 받아옴
        if (user == null)
            user = FirebaseAuth.getInstance().getCurrentUser();
        //  root/customer 하위 user 고유 폴더 명 (user의 UID)
        if (userPath == null)
            userPath = user.getDisplayName() + "_" + user.getUid();
        //  현재 유저의 폴더 Firebase에서의 Reference
        if (myRef == null)
            myRef = FirebaseDatabase.getInstance().getReference().child("customer").child(userPath);
        //  root/trade의 Reference
        if (tradeRef == null)
            tradeRef = FirebaseDatabase.getInstance().getReference().child("trade");
        //Length 변수 초기화

        //tradeRef 탐색하면서 trade 객체의 정보 받아와 tradeListVector에 trade 객체 추가하는 Event Listener
        tradeEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Book book = dataSnapshot.child("book").getValue(Book.class);
                Trade trade = dataSnapshot.getValue(Trade.class);
                trade.setBook(book);
                tradeListVector.add(trade);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //  Vector 초기화
        if (tradeListVector == null)
            tradeListVector = new Vector<Trade>();
        //  어느 RecyclerView에 사용할지에 따라 초기화 실행
        if (whichRecyclerView == WhichRecyclerView.sellRecyclerView || whichRecyclerView == WhichRecyclerView.ongoingRecyclerView || whichRecyclerView == WhichRecyclerView.doneRecyclerView) {
            //  sellListVector 초기화 (selllFragment와 tradeFragment에서 모두 사용)
            //  현재 User가 seller로 등록되어 있는 trade들의 key 값을 Vector로 저장
            if (sellListVector == null) {
                sellListVector = new Vector<String>();
                //  myRef/sellList 에 등록할 Event Listener(sellList에 있는 trade의 key값들을 받아와 sellListVector에 저장)
                ValueEventListener sellListListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //  myRef/sellList의 child들을 탐색
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String str = (String) postSnapshot.getValue();
                            //  얻어온 trade key를 sellListVector에 추가
                            sellListVector.addElement(str);
                            //  root/trade/key값 경로에 tradeEventListener 추가
                            tradeRef.child(str).addListenerForSingleValueEvent(tradeEventListener);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                myRef.child("sellList").addListenerForSingleValueEvent(sellListListener);

            }
            //buyListVector 초기화
            if (whichRecyclerView == WhichRecyclerView.ongoingRecyclerView || whichRecyclerView == WhichRecyclerView.doneRecyclerView) {
                //  buyListVector 초기화 (tradeFragment에서 사용)
                //  현재 User가 purchaser로 등록되어 있는 trade들의 key 값을 Vector로 저장
                if (buyListVector == null) {
                    buyListVector = new Vector<String>();
                    //  myRef/buyList 에 등록할 Event Listener(buyList에 있는 trade의 key값들을 받아와 buyListVector에 저장)
                    ValueEventListener buyListListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //  myRef/buyList의 child들을 탐색
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                String str = (String) postSnapshot.getValue();
                                //  얻어온 trade key를 buyListVector에 추가
                                buyListVector.addElement(str);
                                //  root/trade/key값 경로에 tradeEventListener 추가
                                tradeRef.child(str).addListenerForSingleValueEvent(tradeEventListener);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };
                    myRef.child("buyList").addListenerForSingleValueEvent(buyListListener);

                }
            }
        }
    }

    protected FirebaseCommunicator(Parcel in) {
        user = in.readParcelable(FirebaseUser.class.getClassLoader());
        userPath = in.readString();
    }

    public String getUserPath() {
        return userPath;
    }

    public Vector<Trade> getTradeListVector() {
        return tradeListVector;
    }

    //  RecyclerView 세팅 함수 (어떤 recyclerView인지 넘겨받아 해당 Vector를 adapter에 등록)
    public void setRecyclerView(final Context con, Activity act, RecyclerView recView, final WhichRecyclerView whichRecyclerView) {
        //  초기화
        this.context = con;
        this.activity = act;
        this.recyclerView = recView;
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this.context));
        this.whichRecyclerView = whichRecyclerView;

        //  root/trade의 Event 처리 Listener
        tradeRef.addChildEventListener(new ChildEventListener() {
            //  root/trade에 새로운 child가 추가됐을 경우
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //  trade 객체 생성 후 firebase에서 값 읽어옴
                Book book = dataSnapshot.child("book").getValue(Book.class);
                Trade trade = dataSnapshot.getValue(Trade.class);
                trade.setBook(book);
                //  sellFragment에서 사용할 경우
                if (whichRecyclerView == WhichRecyclerView.sellRecyclerView) {
                    //  Adapter 등록
                    //recyclerView.setAdapter(new RecyclerViewTradeAdapter(context, tradeListVector));
                    recyclerView.swapAdapter(new RecyclerViewTradeAdapter(context, tradeListVector), true);
                } else if (whichRecyclerView == WhichRecyclerView.ongoingRecyclerView || whichRecyclerView == WhichRecyclerView.ongoingRecyclerView) {

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (whichRecyclerView == WhichRecyclerView.sellRecyclerView) {
                    recyclerView.setAdapter(new RecyclerViewTradeAdapter(context, tradeListVector));
                } else if (whichRecyclerView == WhichRecyclerView.ongoingRecyclerView || whichRecyclerView == WhichRecyclerView.ongoingRecyclerView) {

                }
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
        this.recyclerView.smoothScrollToPosition(0);
    }

    public String timeToString() {
        Calendar c = Calendar.getInstance();
        String str;
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        str = String.valueOf(year);
        if (month < 10)
            str = str + "-0" + String.valueOf(month);
        else
            str = str + "-" + String.valueOf(month);

        if (day < 10)
            str = str + "-0" + String.valueOf(day);
        else
            str = str + "-" + String.valueOf(day);

        if (hour < 10)
            str = str + "-0" + String.valueOf(hour);
        else
            str = str + "-" + String.valueOf(hour);

        if (minute < 10)
            str = str + ":0" + String.valueOf(minute);
        else
            str = str + ":" + String.valueOf(minute);

        if (second < 10)
            str = str + ":0" + String.valueOf(second);
        else
            str = str + ":" + String.valueOf(second);

        return str;
    }

    //Trade 객체 업로드 함수
    public void uploadTrade(final Trade trade) {
        //root/trade 밑에 push할 때의 key 값 얻음
        String key = tradeRef.push().getKey();
        //Upload할 trade 객체의 tradeId에 key 값 입력
        trade.setTradeId(key);
        //trade의 book 객체 Map으로 변환
        Map<String, Object> bookMap = new HashMap<>();
        trade.getBook().toMap(bookMap);
        //trade 객체 Map으로 변환
        Map<String, Object> tradeMap = new HashMap<>();
        trade.toMap(tradeMap);
        //book 객체의 정보 Frirebase에 Upload
        tradeRef.child(key).child("book").updateChildren(bookMap);
        //trade 객체의 정보 FIrebase에 Upload
        tradeRef.child(key).updateChildren(tradeMap);

        //현재 로그인한 사용자의 sellList Update 위해 기존의 sellListVector에 현재 trade의 key 값 추가
        sellListVector.add(key);
        tradeListVector.add(trade);
        //Update된 sellListVector를 FIrebase에 Upload
        myRef.child("sellList").setValue(sellListVector);
        return;
    }

    //CustomerId로 Customer 객체를 Return하는 함수
    public Customer getCustomer(String customerId) {
        final Customer[] customer = {new Customer()};
        FirebaseDatabase.getInstance().getReference().child("customer").child(customerId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        customer[0] = dataSnapshot.getValue(Customer.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
        return customer[0];
    }

}
