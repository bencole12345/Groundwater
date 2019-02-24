package uk.ac.cam.cl.juliet.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.cl.juliet.R;
import uk.ac.cam.cl.juliet.views.AttenuatorConfigurationView;

/**
 * A fullscreen dialog for inputting an arbitrary number of (attenuator, gain) pairs.
 *
 * @author Ben Cole
 */
public class AttenuatorsDialog extends DialogFragment implements View.OnClickListener {

    private Button closeButton;
    private Button doneButton;
    private Button addAttenuatorButton;
    private Button removeAttenuatorButton;

    private LinearLayout attenuatorsContainer;
    private List<AttenuatorConfigurationView> attenuatorsList;

    private OnAttenuatorsSelectedListener listener;

    public void setOnAttenuatorsSelectedListener(OnAttenuatorsSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
        // TODO: find a way to pass in the existing settings
        attenuatorsList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_attenuators_fullscreen, container, false);
        closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);
        doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(this);
        addAttenuatorButton = view.findViewById(R.id.addAttenuatorButton);
        addAttenuatorButton.setOnClickListener(this);
        removeAttenuatorButton = view.findViewById(R.id.removeAttenuatorButton);
        removeAttenuatorButton.setOnClickListener(this);
        attenuatorsContainer = view.findViewById(R.id.attenuatorsContainer);
        addAttenuator();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeButton:
                dismiss();
                break;
            case R.id.doneButton:
                onDone();
                break;
            case R.id.addAttenuatorButton:
                addAttenuator();
                break;
            case R.id.removeAttenuatorButton:
                removeAttenuator();
        }
    }

    /**
     * Passes the selected attenuator and gain values to the callback, then dismisses the dialog.
     */
    private void onDone() {
        if (listener == null) return;
        List<Integer> attenuators = new ArrayList<>();
        List<Integer> gains = new ArrayList<>();
        for (AttenuatorConfigurationView v : attenuatorsList) {
            attenuators.add(v.getAttenuation());
            attenuators.add(v.getGain());
        }
        listener.onAttenuatorsSelected(attenuators, gains);
        dismiss();
    }

    /**
     * Adds another attenuator to the UI.
     *
     * <p>If this action causes there to be more than 1 attenuator, the "remove attenuator" button
     * will now be re-enabled.
     */
    private void addAttenuator() {

        // Create another attenuator
        AttenuatorConfigurationView view = new AttenuatorConfigurationView(getContext());
        attenuatorsList.add(view);
        attenuatorsContainer.addView(view);

        // If needed, re-enable the "remove attenuator" button
        if (attenuatorsList.size() > 1) {
            removeAttenuatorButton.setEnabled(true);
        }
    }

    /**
     * Removes the bottom attenuator from the UI.
     *
     * <p>If this leaves only one attenuator remaining, then the "remove attenuator" button will be
     * disabled: this prevents creating a configuration with no attenuator values.
     */
    private void removeAttenuator() {
        // TODO: implement
        // check there are more than 1
        // remove entry from lists
        // check if we need to disable the remove button

        // Prevent having fewer than one attenuator value
        if (attenuatorsList.size() <= 1) return;

        // Remove the most recent view
        attenuatorsList.remove(attenuatorsList.size() - 1);
        attenuatorsContainer.removeViewAt(attenuatorsList.size());

        // If needed, disable the "remove attenuator" button
        boolean removeButtonEnabled = (attenuatorsList.size() > 1);
        removeAttenuatorButton.setEnabled(removeButtonEnabled);
    }

    public interface OnAttenuatorsSelectedListener {
        void onAttenuatorsSelected(List<Integer> attenuators, List<Integer> gains);
    }
}
