package ch.rmy.android.http_shortcuts.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.activities.ChooserActivity;
import ch.rmy.android.http_shortcuts.activities.EditorActivity;

public class ChooserAdapter extends RecyclerView.Adapter<ChooserAdapter.ChooserHolder>{

    private List<ApplicationInfo> listOfApps;
    private Context mContext;
    private PackageManager pm;
    private OnItemClickListener listener;


    public ChooserAdapter(@NonNull Context context, List<ApplicationInfo> list, OnItemClickListener l) {
        this.listOfApps = list;
        this.mContext = context;
        this.pm = context.getPackageManager();
        this.listener = l;
//                new ChooserAdapter.OnItemClickListener(){
//            @Override
//            public void onItemClick(ApplicationInfo app){
//                try{
//                    Intent intent = (new EditorActivity.IntentBuilder(mContext)).build();
//                    intent.putExtra("package_name", app.packageName);
//                    ((ChooserActivity)mContext).startActivityForResult(intent, 1);
//                }catch (Exception e){
//                    Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            }
//        };
    }

    public class ChooserHolder extends RecyclerView.ViewHolder{
        private ImageView packageImage;
        private TextView packageName;

        ChooserHolder(View v){
            super(v);
            this.packageImage = (ImageView) v.findViewById(R.id.app_image);
            this.packageName = (TextView) v.findViewById(R.id.app_package_name);
        }

        public void setDetails(final ApplicationInfo app) {
            packageName.setText(app.loadLabel(mContext.getPackageManager()));

            try{
                packageImage.setImageDrawable(app.loadIcon(pm));
            }catch (Exception e){
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @NonNull
    @Override
    public ChooserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.chooser_item_list, parent, false);
        return new ChooserHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChooserHolder holder, int position) {
        holder.setDetails(listOfApps.get(position));
        setListeners(holder, position);
    }

    @Override
    public int getItemCount() {
        return listOfApps.size();
    }

    /*-------->| Listerner |<--------*/
//    private void setListeners(ChooserHolder holder, final int position) {
//        holder.packageImage.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                String packageName = listOfApps.get(position).packageName;
//
//                Intent i = new Intent(mContext, EditorActivity.class);
//                i.putExtra("package_name", packageName);
//                ((ChooserActivity)mContext).startActivityForResult(i, 1);
//            }
//        });
//    }

    private void setListeners(ChooserHolder holder, final int position) {
        holder.packageImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(listener == null){
                    Toast.makeText(mContext, "listener is NULL", Toast.LENGTH_LONG).show();
                }
                else
                listener.onItemClick(listOfApps.get(position));
            }
        });
    }


    public interface OnItemClickListener {
        public void onItemClick(ApplicationInfo app);
    }
}
