package cabelino.noticiasCampusVitoria;

/**
 * Created by cabelino on 13/02/17.
 */

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cabelino.meuprimeiroapp.R;


public class ListViewAdapter extends ArrayAdapter<ApresentaNoticia> {
    // Declare Variables
    Context context;
    LayoutInflater inflater;
    List<ApresentaNoticia> listadenoticias;
    private SparseBooleanArray mSelectedItemsIds;

    public ListViewAdapter(Context context, int resourceId,
                           List<ApresentaNoticia> worldpopulationlist) {
        super(context, resourceId, worldpopulationlist);
        mSelectedItemsIds = new SparseBooleanArray();
        this.context = context;
        this.listadenoticias = worldpopulationlist;
        inflater = LayoutInflater.from(context);
    }

    private class ViewHolder {
        TextView rank;
        TextView country;
        TextView population;
        ImageView flag;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.listview_item, null);
            // Locate the TextViews in listview_item.xml
            holder.rank = (TextView) view.findViewById(R.id.rank);
            holder.country = (TextView) view.findViewById(R.id.country);
            // Locate the ImageView in listview_item.xml
            holder.flag = (ImageView) view.findViewById(R.id.flag);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Capture position and set to the TextViews
        holder.rank.setText(listadenoticias.get(position).getTitulo());
        holder.country.setText(listadenoticias.get(position).getTexto());
        // Capture position and set to the ImageView
        holder.flag.setImageResource(listadenoticias.get(position)
                .getFlag());
        return view;
    }

    @Override
    public void remove(ApresentaNoticia object) {
        listadenoticias.remove(object);
        notifyDataSetChanged();
    }

    public List<ApresentaNoticia> getWorldPopulation() {
        return listadenoticias;
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}