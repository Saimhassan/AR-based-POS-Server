package saim.hassan.arfypserver.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import saim.hassan.arfypserver.Common.Common;
import saim.hassan.arfypserver.Interface.ItemClickListener;
import saim.hassan.arfypserver.R;

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener


{

    public TextView textcategorye;
    public ImageView imagecategory;

    public ItemClickListener itemClickListener;

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);

        textcategorye = (TextView)itemView.findViewById(R.id.category_text);
        imagecategory = (ImageView)itemView.findViewById(R.id.category_image);
        itemView.setOnCreateContextMenuListener(this);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v,getAdapterPosition(),false);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select Action");
        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(),Common.DELETE);

    }
}
