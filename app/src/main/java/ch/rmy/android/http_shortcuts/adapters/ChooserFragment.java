package ch.rmy.android.http_shortcuts.adapters;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ch.rmy.android.http_shortcuts.R;

public class ChooserFragment extends Fragment {

    private FloatingActionButton backButton;
    private TextView message_tv;
    private RecyclerView recyclerView;

    TopSectionListener activityComander;

    public interface TopSectionListener{
        public void removeFragment();
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            activityComander = (TopSectionListener) activity;
        }catch (Exception e){
            Toast.makeText(getContext(), "onAtach error", Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chooser_list, container, false);
        List<ApplicationInfo> list = getContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

        backButton = (FloatingActionButton) v.findViewById(R.id.back_button_2);
        message_tv = (TextView) v.findViewById(R.id.message_list_apps);
        recyclerView = (RecyclerView) v.findViewById(R.id.device_recycle_view);

        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //remove fragment
                activityComander.removeFragment();
            }
        });

        return v;
    }
}
