package ssu.ssu.huncheckwhatssu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;

import java.util.Vector;

import ssu.ssu.huncheckwhatssu.DB.DBHelper;
import ssu.ssu.huncheckwhatssu.utilClass.Customer;
import ssu.ssu.huncheckwhatssu.utilClass.Trade;

public class RecyclerViewTradeAdapter_Sell extends RecyclerView.Adapter<RecyclerViewTradeAdapter_Sell.TradeViewHolder> {
    LayoutInflater inflater;
    Vector<Trade> modelVector;
    RecyclerView recyclerView;
    TextView countView;

    public RecyclerViewTradeAdapter_Sell(Context context,  Vector<Trade> vector, RecyclerView recyclerView, TextView countView) {
        this.inflater = LayoutInflater.from(context);
        this.modelVector = vector;
        this.recyclerView = recyclerView;
        this.countView = countView;

    }

    @Override
    public TradeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.trade_item_fragment_sell, parent, false);
        return new TradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TradeViewHolder holder, int position) {
        holder.bindData(modelVector.get(position));
    }

    public Vector<Trade> getTrades(){ return modelVector;}

    @Override
    public int getItemCount() {
        return modelVector.size();
    }

    class TradeViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView bookTitleTextView;
        TextView sellerNameTextView;
        TextView originalPriceTextView;
        TextView sellingPriceTextView;
        TextView bookCategoryTextView;
        TextView bookAuthorTextView;
        TextView bookPublisherTextView;
        TextView sellerCreditTextView;
        TextView purchaseRequestCountView;

        public TradeViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            bookTitleTextView = itemView.findViewById(R.id.item_book_title);
            sellerNameTextView = itemView.findViewById(R.id.item_seller_name);
            originalPriceTextView = itemView.findViewById(R.id.item_book_original_price);
            sellingPriceTextView =itemView.findViewById(R.id.item_book_selling_price);
            bookCategoryTextView = itemView.findViewById(R.id.item_book_category);
            bookAuthorTextView = itemView.findViewById(R.id.item_book_author);
            bookPublisherTextView = itemView.findViewById(R.id.item_book_publisher);
            sellerCreditTextView = itemView.findViewById(R.id.item_seller_credit);
            purchaseRequestCountView = itemView.findViewById(R.id.item_purchase_request_count);
            originalPriceTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

        }

        public void bindData(Trade object) {
            if(object.getSeller().getName() == null) {
                object.setSeller(new Customer(object.getSellerId()));
                object.getSeller().setCustomerDataFromUID(recyclerView.getAdapter());
            }
            if (object.getBook().getImage() == null) {
                imageView.setImageDrawable(itemView.getResources().getDrawable(R.drawable.noimage));
            } else Glide.with(itemView).load(object.getBook().getImage()).into(imageView);
            bookTitleTextView.setText(object.getBook().getTitle());
            sellerNameTextView.setText(object.getSeller().getName());
            originalPriceTextView.setText(String.valueOf(object.getBook().getOriginalPrice()));
            sellingPriceTextView.setText(String.valueOf(object.getSellingPrice()));
            bookAuthorTextView.setText(object.getBook().getAuthor());
            bookPublisherTextView.setText(object.getBook().getPublisher());
            sellerCreditTextView.setText(""+object.getSeller().getCreditRating());
            DBHelper dbHelper = new DBHelper(inflater.getContext());
            bookCategoryTextView.setText(dbHelper.getFullCategoryText(object.getBook()));
            countView.setText(getItemCount() + " 건");
            purchaseRequestCountView.setText("요청:0");
            FirebaseHelper firebaseHelper = new FirebaseHelper();
            firebaseHelper.addCallBackListener(new FirebaseHelper.CallBackListener() {
                @Override
                public void afterGetCustomer(Customer customer) {
                }

                @Override
                public void afterGetPurchaseRequestCount(int count) {
                    purchaseRequestCountView.setText("요청:"+Integer.toString(count));
                }
            });
            firebaseHelper.getPurchaseRequestCount(object.getTradeId());
        }
    }

    //RecyclerView에 TouchListener 설정 함수 (Swipe로 메뉴 출력 가능하게)
    public void setSwipeable(final Context context, final Activity activity, final Fragment fragment, final RecyclerView recyclerView) {
        RecyclerTouchListener onTouchListener = new RecyclerTouchListener(activity, recyclerView);
        onTouchListener
                .setClickable(new RecyclerTouchListener.OnRowClickListener() {
                    @Override
                    public void onRowClicked(int position) {
                        Trade trade = ((RecyclerViewTradeAdapter_Sell)(recyclerView.getAdapter())).getTrades().get(position);
                        recyclerView.getAdapter().notifyItemChanged(position);
                        Intent intent=new Intent(context,BookInfoActivity.class);
                        intent.putExtra("BookInfoType","BOOK_INFO_DEFAULT");
                        intent.putExtra("fragment", "sell");
                        intent.putExtra("book_info_default_data", trade);
                        context.startActivity(intent);
                        /*여기에 액티비티로 전달하는 기능이 구현되있어야함.*/
                    }

                    @Override
                    public void onIndependentViewClicked(int independentViewID, int position) {
                        Toast toast = Toast.makeText(context, "IndependentViewID", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                })
                .setSwipeOptionViews(R.id.item_button_notification, R.id.item_button_edit, R.id.item_button_delete)
                .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {

                    @Override
                    public void onSwipeOptionClicked(int viewID, final int position) {
                        final Trade trade = ((RecyclerViewTradeAdapter_Sell)(recyclerView.getAdapter())).getTrades().get(position);
                        if (viewID == R.id.item_button_notification) {
                            Intent intent = new Intent(context.getApplicationContext(),SelectPurchaserActivity.class);
                            intent.putExtra("tradeKey",trade.getTradeId());
                            intent.putExtra("sellerId",trade.getSellerId());
                            intent.putExtra("position", position);
                            fragment.startActivityForResult(intent, 1);
//                            Toast toast = Toast.makeText(activity, "구매요청!", Toast.LENGTH_SHORT);
//                            toast.show();
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                        else if (viewID == R.id.item_button_edit) {
                            Intent intent=new Intent(context, EditSell.class);
                            intent.putExtra("activity", "SellFragment");
                            intent.putExtra("editTrade", trade);
                            intent.putExtra("position", position);
                            fragment.startActivityForResult(intent, 1);

                            recyclerView.getAdapter().notifyItemChanged(position);
                        } else if (viewID == R.id.item_button_delete) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(context);
                            alert.setTitle("판매 등록 취소");
                            alert.setMessage("정말로 판매를 취소 하시겠습니까?");
                            alert.setPositiveButton("등록 취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseHelper firebaseHelper = new FirebaseHelper();
                                    firebaseHelper.deletePurchaseRequest(trade.getTradeId());
                                    FirebaseCommunicator.deleteTrade(trade);
                                    ((RecyclerViewTradeAdapter_Sell) (recyclerView.getAdapter())).getTrades().remove(position);
                                    recyclerView.getAdapter().notifyItemRemoved(position);
                                    recyclerView.getAdapter().notifyDataSetChanged();
                                    countView.setText(recyclerView.getAdapter().getItemCount() + " 건");
                                    Toast toast = Toast.makeText(context, "거래삭제", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });
                            alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

                                    Toast toast = Toast.makeText(context, "취소", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });
                            alert.show();
                        }
                    }
                });
        recyclerView.addOnItemTouchListener(onTouchListener);
        return;
    }

}