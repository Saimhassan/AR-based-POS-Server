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

public class BannerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener
{

    public TextView textbanner;
    public ImageView imagebanner;
    public BannerViewHolder(@NonNull View itemView) {
        super(itemView);

        textbanner = (TextView)itemView.findViewById(R.id.banner_text);
        imagebanner = (ImageView)itemView.findViewById(R.id.banner_image);
        itemView.setOnCreateContextMenuListener(this);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select Action");
        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(),Common.DELETE);

    }

    @Override
    public void onClick(View v) {
    }
}

