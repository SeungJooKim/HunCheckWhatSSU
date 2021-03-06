package ssu.ssu.huncheckwhatssu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

import ssu.ssu.huncheckwhatssu.DB.DBHelper;
import ssu.ssu.huncheckwhatssu.utilClass.Trade;

public class RecyclerViewTradeAdapter_Search extends RecyclerView.Adapter<RecyclerViewTradeAdapter_Search.TradeViewHolder> {
    LayoutInflater inflater;
    static List<Trade> modelList;

    public RecyclerViewTradeAdapter_Search(Context context, List<Trade> modelList) {
        inflater = LayoutInflater.from(context);
        this.modelList = modelList;
    }

    @Override
    public TradeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.trade_item_search, parent, false);

        return new TradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TradeViewHolder holder, int position) {
        holder.bindData(modelList.get(position));
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class TradeViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView bookTitleTextView;
        TextView bookPriceTextView;
        TextView sellerNameTextView;
        TextView bookAuthorTextView;
        TextView bookCategoryTextView;
        TextView bookPublisherTextView;
        TextView bookSellingPriceTextView;
        TextView sellerCreditTextView;
        TextView tradeState;
        TextView tradeDate;

        public TradeViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.item_image);
            bookTitleTextView = itemView.findViewById(R.id.item_book_title);
            bookPriceTextView = itemView.findViewById(R.id.item_book_original_price);

            bookPriceTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

            sellerNameTextView = itemView.findViewById(R.id.item_seller_name);
            bookAuthorTextView = itemView.findViewById(R.id.item_book_author);
            bookCategoryTextView= itemView.findViewById(R.id.item_book_category);
            bookPublisherTextView = itemView.findViewById(R.id.item_book_publisher);
            bookSellingPriceTextView = itemView.findViewById(R.id.item_book_selling_price);
            sellerCreditTextView = itemView.findViewById(R.id.item_seller_credit);

            tradeState = itemView.findViewById(R.id.item_trade_state);
            tradeDate = itemView.findViewById(R.id.item_seller_sell_date);
        }

        public void bindData(Trade object) {
            if (object == null) return;

            Log.d("JS", "bindData: " + object.toString());

//            imageView.setBackgroundResource(R.drawable.bookimag);
            bookTitleTextView.setText(object.getBook().getTitle());
            bookPriceTextView.setText(String.valueOf(object.getBook().getOriginalPrice()));
            bookSellingPriceTextView.setText(String.valueOf(object.getSellingPrice()));
            bookPublisherTextView.setText(String.valueOf(object.getBook().getPublisher()));

            DBHelper dbHelper = new DBHelper(inflater.getContext());
            StringBuilder sb = new StringBuilder();


            sb.append(dbHelper.getCollegeName(object.getBook().getCollege_id()));

            sb.append(">");

            sb.append(dbHelper.getDepartmentName(object.getBook().getDepartment_id()));

            sb.append(">");

            sb.append(dbHelper.getSubjectName(object.getBook().getSubject_id()));


            bookCategoryTextView.setText(sb.toString());
            bookAuthorTextView.setText(String.valueOf(object.getBook().getAuthor()));
            sellerCreditTextView.setText(String.format("%.2f", object.getSeller().getCreditRating()));
            sellerNameTextView.setText(object.getSeller().getName());
            tradeState.setText(object.getTradeStateForShowView());

            if (object.getBook().getImage() != null)
                Glide.with(inflater.getContext()).load(object.getBook().getImage()).into(imageView);
            else
                imageView.setImageDrawable(inflater.getContext().getResources().getDrawable(R.drawable.noimage));
        }
    }

}