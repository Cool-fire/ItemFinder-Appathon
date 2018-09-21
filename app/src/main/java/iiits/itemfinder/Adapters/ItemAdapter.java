package iiits.itemfinder.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;

import iiits.itemfinder.R;
import iiits.itemfinder.models.Item;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.support.constraint.Constraints.TAG;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private final RealmResults<Item> items;
    private Context context;
    private ImageView snippetImage;
    private ProgressBar snippetimageProgress;
    private File imageFile;

    public ItemAdapter(RealmResults<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View ItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemlistrow,parent,false);
        return new ItemAdapter.ViewHolder(ItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
            Item item = items.get(position);
            holder.itemName.setText(item.getItemName().toString());
            holder.itemPlace.setText(item.getPlace().toString());
            holder.itemdate.setText(item.getDate().toString());
            snippetImage = holder.itemImage;
            snippetimageProgress = holder.snippetimageProgress;
        snippetimageProgress.setVisibility(View.VISIBLE);

        try
        {
            imageFile = new File(item.getImagePath().toString());
            if(imageFile.exists())
            {
                Picasso.get().load(imageFile).resize(context.getResources().getDimensionPixelSize(R.dimen.snippet_thumb_width),
                        context.getResources().getDimensionPixelSize(R.dimen.snippet_thumb_height)).centerCrop().into(snippetImage);
                snippetimageProgress.setVisibility(View.INVISIBLE);
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "onBindViewHolder: error");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void deleteItem(Realm realm, final int position) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                items.deleteFromRealm(position);
            }
        });
        notifyDataSetChanged();
        notifyItemRangeChanged(position,getItemCount());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemName;
        private final TextView itemPlace;
        private final ImageView itemImage;
        private final ImageButton itemLocation;
        private final TextView itemdate;
        private final ProgressBar snippetimageProgress;

        public ViewHolder(View itemView) {
            super(itemView);
            itemName =(TextView)itemView.findViewById(R.id.itemNames);
            itemPlace = (TextView)itemView.findViewById(R.id.list_place);
            itemImage = (ImageView)itemView.findViewById(R.id.itemimage);
            itemdate = (TextView)itemView.findViewById(R.id.itemDate);
            itemLocation = (ImageButton)itemView.findViewById(R.id.locationListBttn);
            snippetimageProgress = (ProgressBar)itemView.findViewById(R.id.progressBarId);

        }
    }
}
