package edu.miami.cs.jadedo.phlogging;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;
import android.net.Uri;
import android.app.Activity;
import android.content.DialogInterface;

public class DialogOfPhlogEntry extends DialogFragment implements DialogInterface.OnDismissListener {

    View dialogView;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ImageView fullImage;

        dialogView = inflater.inflate(R.layout.dialog_phlog_entry_view, container );

        fullImage = (ImageView) dialogView.findViewById(R.id.dialog_image);

        return dialogView;
    }
}
