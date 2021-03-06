package uk.ac.cam.cl.juliet.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.cam.cl.juliet.R;
import uk.ac.cam.cl.juliet.computationengine.plotdata.PlotData3D;
import uk.ac.cam.cl.juliet.computationengine.plotdata.PlotDataGenerator3D;
import uk.ac.cam.cl.juliet.connection.ConnectionSimulator;
import uk.ac.cam.cl.juliet.data.InternalDataHandler;
import uk.ac.cam.cl.juliet.models.Datapoint;
import uk.ac.cam.cl.juliet.models.MultipleBurstsDataTypes;
import uk.ac.cam.cl.juliet.models.SingleOrManyBursts;
import uk.ac.cam.cl.juliet.tasks.ILiveProcessingTask;
import uk.ac.cam.cl.juliet.tasks.IProcessingCallback;
import uk.ac.cam.cl.juliet.tasks.LiveProcessingTask;
import uk.ac.cam.cl.juliet.tasks.ProcessingTask;

/** Displays more detail about the currently open data file. */
public class InfoMoreDetailFragment extends Fragment
        implements ILiveProcessingTask, ConnectionSimulator.ConnectionListener {

    private final int BURST_CODE = 1;
    private final int JAVASCRIPT_BATCH_SIZE = 10000;

    private WebView webview;
    private TextView webviewText;
    private InternalDataHandler idh;
    private Map<String, List<PlotDataGenerator3D>> cache;
    private MenuItem startMeasuringButton;
    private MenuItem stopMeasuringButton;
    private ConnectionSimulator simulator;
    private boolean connected;
    private boolean gatheringData;
    private SingleOrManyBursts currentLiveBursts;
    private Spinner detailedSpinner;
    private ProgressBar liveProgressBar;
    private TextView noFilesToPlotMessage;
    private ProgressBar generatingSpinner;
    private TextView generatingText;
    private boolean dataHasBeenPlottedAtLeastOnce;

    private List<File> prevAndCurrent;
    boolean firstTime;

    /** The state that the UI is in. */
    private enum State {

        /** The initial state: only the "no files selected" message will be displayed. */
        INITIAL,

        /** Displays the "generating plot" text and progress spinner. */
        PROCESSING,

        /** Only the WebView will be displayed. */
        STATIC_COLLECTION_DISPLAYED,

        /**
         * The WebView and horizontal progress bar are displayed, to make it clear that data is
         * still being received.
         */
        COLLECTING_LIVE_DATA
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_detail, container, false);

        // Menu boolean
        setHasOptionsMenu(true);
        this.connected = false;
        this.gatheringData = false;
        dataHasBeenPlottedAtLeastOnce = false;

        // Initialise text
        webviewText = view.findViewById(R.id.webview_title);

        // Initialise cache
        cache = new HashMap<>();

        // Initialise webview
        webview = view.findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setWebChromeClient(new WebChromeClient());

        // Initialise spinner
        detailedSpinner = view.findViewById(R.id.detailed_spinner);
        String[] datatypes =
                new String[] {
                    MultipleBurstsDataTypes.POWER.getDisplayableName(),
                    MultipleBurstsDataTypes.PHASE.getDisplayableName()
                };
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        getContext(), R.layout.support_simple_spinner_dropdown_item, datatypes);
        detailedSpinner.setAdapter(adapter);

        // Set the spinners listener
        detailedSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        updateChart();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

        idh = InternalDataHandler.getInstance();

        // Listen for file changes
        idh.addCollectionListener(
                new InternalDataHandler.FileListener() {
                    @Override
                    public void onChange() {
                        webview.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        updateChart();
                                    }
                                });
                    }
                });

        // Get UI handles
        liveProgressBar = view.findViewById(R.id.progressBar);
        noFilesToPlotMessage = view.findViewById(R.id.noFilesToPlotMessage);
        generatingSpinner = view.findViewById(R.id.generatingSpinner);
        generatingText = view.findViewById(R.id.generatingText);

        // Listener for connection changes
        ConnectionSimulator simulator = ConnectionSimulator.getInstance();
        simulator.addListener(this);

        updateUIState(State.INITIAL);

        return view;
    }

    /**
     * Shows and hides UI elements based on the new state.
     *
     * @param state The new state of the UI
     */
    private void updateUIState(State state) {
        switch (state) {
            case INITIAL:
                webview.setVisibility(View.INVISIBLE);
                liveProgressBar.setVisibility(View.INVISIBLE);
                noFilesToPlotMessage.setVisibility(View.VISIBLE);
                generatingSpinner.setVisibility(View.INVISIBLE);
                generatingText.setVisibility(View.INVISIBLE);
                return;
            case PROCESSING:
                webview.setVisibility(View.INVISIBLE);
                liveProgressBar.setVisibility(View.INVISIBLE);
                noFilesToPlotMessage.setVisibility(View.INVISIBLE);
                generatingSpinner.setVisibility(View.VISIBLE);
                generatingText.setVisibility(View.VISIBLE);
                return;
            case STATIC_COLLECTION_DISPLAYED:
                webview.setVisibility(View.VISIBLE);
                liveProgressBar.setVisibility(View.INVISIBLE);
                noFilesToPlotMessage.setVisibility(View.INVISIBLE);
                generatingSpinner.setVisibility(View.INVISIBLE);
                generatingText.setVisibility(View.INVISIBLE);
                return;
            case COLLECTING_LIVE_DATA:
                webview.setVisibility(View.VISIBLE);
                liveProgressBar.setVisibility(View.VISIBLE);
                noFilesToPlotMessage.setVisibility(View.INVISIBLE);
                generatingSpinner.setVisibility(View.INVISIBLE);
                generatingText.setVisibility(View.INVISIBLE);
        }
    }

    /** Decides whether to show the "start measuring" button and updates the UI accordingly. */
    private void updateMeasureVisibility() {
        if (startMeasuringButton != null) {
            startMeasuringButton.setVisible(connected && !gatheringData);
        }
        if (stopMeasuringButton != null) {
            stopMeasuringButton.setVisible(connected && gatheringData);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_data, menu);
        startMeasuringButton = menu.findItem(R.id.start_measuring_button);
        stopMeasuringButton = menu.findItem(R.id.stop_measuring_button);
        // Only have connect visible if we aren't running any data gathering
        updateMeasureVisibility();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_measuring_button:
                startGatheringData();
                return true;
            case R.id.stop_measuring_button:
                stopGatheringData();
                return true;
        }
        return false;
    }

    /**
     * This method checks to see if we have the right type of file selected and then runs the
     * processing if we haven't already cached the results from a previous round of processing. The
     * uniqueness in the cache is dependent on the file/directory name.
     */
    private void updateChart() {
        if (checkFile()) {
            updateUIState(State.PROCESSING);
            InternalDataHandler idh = InternalDataHandler.getInstance();
            // Create datapoints, json-ise and pass to Javascript
            webviewText.setText(idh.getCollectionSelected().getNameToDisplay());

            List<Datapoint> datapoints = new ArrayList<>();
            if (!cache.containsKey(idh.getCollectionSelected().getNameToDisplay())) {
                ProcessingTask task = new ProcessingTask(this);
                task.execute();
            } else {
                List<PlotDataGenerator3D> generators =
                        cache.get(idh.getCollectionSelected().getNameToDisplay());
                datapoints = generateDatapoints(generators);
                updateWebview(datapoints);
            }
        }
    }

    /**
     * Checking for many bursts
     *
     * @return <code>boolean</code> if it is a many-burst file
     */
    private boolean checkFile() {
        InternalDataHandler idh = InternalDataHandler.getInstance();
        if (idh.getCollectionSelected() == null) return false;
        return idh.getCollectionSelected().getIsManyBursts();
    }

    /**
     * A method for passing the datapoints to the webview and JSON-ising them
     *
     * @param datapoints
     */
    private void updateWebview(final List<Datapoint> datapoints) {
        webview.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        // After the HTML page loads, run JS to initialize graph
                        Gson gson = new Gson();

                        // Convert the data to json which the D3 can handle
                        int i = 1;
                        List<Datapoint> temp = new ArrayList<>();
                        for (Datapoint datapoint : datapoints) {
                            temp.add(datapoint);
                            // URLs have a maximum length so we must load the data in batches to the
                            // JavaScript
                            if (i % JAVASCRIPT_BATCH_SIZE == 0) {
                                webview.loadUrl("javascript:loadData(" + gson.toJson(temp) + ")");
                                temp.clear();
                            }
                            i++;
                        }

                        // Load in the last batch and init the graph
                        webview.loadUrl("javascript:loadData(" + gson.toJson(temp) + ")");
                        webview.loadUrl("javascript:initGraph()");
                    }
                });
        // Load base html from the assets directory
        webview.loadUrl("file:///android_asset/html/graph.html");
        dataHasBeenPlottedAtLeastOnce = true;
        if (!idh.getProcessingData() && gatheringData) {
            updateUIState(State.COLLECTING_LIVE_DATA);
        } else {
            updateUIState(State.STATIC_COLLECTION_DISPLAYED);
        }
    }

    /** Establishes a connection and waits for the data gathering to commence */
    private void establishConnection() {
        simulator = ConnectionSimulator.getInstance();
    }

    /**
     * Starts the <code>LiveProcessingTask</code>
     *
     * @param previousFile
     * @param currentFile
     * @param lastFile
     */
    private void processLiveData(File previousFile, File currentFile, boolean lastFile) {
        List<IProcessingCallback> listeners = new ArrayList<>();
        listeners.add(this);
        LiveProcessingTask task =
                new LiveProcessingTask(
                        listeners,
                        previousFile,
                        currentFile,
                        this.currentLiveBursts,
                        lastFile,
                        MultipleBurstsDataTypes.fromString(
                                (String) detailedSpinner.getSelectedItem()));
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /** Called to initialise the data gathering phase */
    private void startGatheringData() {
        // Check to see if we can start measuring
        InternalDataHandler idh = InternalDataHandler.getInstance();
        if (idh.getProcessingData()) {
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Currently processing data...", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Clear previous plot
        updateWebview(new ArrayList<Datapoint>());

        ConnectionSimulator simulator = ConnectionSimulator.getInstance();
        prevAndCurrent = new ArrayList<>();
        prevAndCurrent.add(null);
        firstTime = true;
        webview.reload();
        this.gatheringData = true;
        updateUIState(State.COLLECTING_LIVE_DATA);
        updateMeasureVisibility();
        simulator.beginDataGathering();

        if (!this.gatheringData) {
            Context context = getContext();
            if (context != null) {
                Toast.makeText(
                                context,
                                "No files. Please add a folder called data_files.",
                                Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    /** Finishes gathering data */
    private void stopGatheringData() {
        gatheringData = false;
        firstTime = true;
        simulator.stopMeasuring();
        updateMeasureVisibility();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case BURST_CODE:
                {
                    updateChart();
                }
        }
    }

    @Override
    public void onTaskCompleted(
            List<PlotDataGenerator3D> generators, boolean isLive, boolean isLast) {
        InternalDataHandler idh = InternalDataHandler.getInstance();
        // If we are drawing live data then we need to be updating the cached values because we
        // don't yet have them all
        if (isLive) {
            if (cache.containsKey(idh.getCurrentLiveData())) {
                cache.get(idh.getCurrentLiveData()).addAll(generators);
            } else {
                List<PlotDataGenerator3D> data = new ArrayList<>(generators);
                cache.put(idh.getCurrentLiveData(), data);
            }
            updateWebview(generateDatapoints(cache.get(idh.getCurrentLiveData())));
            if (isLast) {
                this.connected = false;
                this.gatheringData = false;
                updateUIState(State.STATIC_COLLECTION_DISPLAYED);
            }
        } else {
            cache.put(idh.getCollectionSelected().getNameToDisplay(), generators);
            idh.setProcessingData(false);
            updateWebview(
                    generateDatapoints(cache.get(idh.getCollectionSelected().getNameToDisplay())));
            updateUIState(State.STATIC_COLLECTION_DISPLAYED);
        }
    }

    /**
     * For generating the datapoints to plot from the <code>PlotDataGenerator3D</code> generators -
     * NOTE: this code assumes the generators are paired in order to get the phase differences and
     * hence the <code>x</code> values start at 1 to get the second generator from each pair that
     * will have phase data.
     *
     * @param generators - the processed data collections
     * @return <code>List<Datapoint></code> - the plottable points
     */
    private List<Datapoint> generateDatapoints(List<PlotDataGenerator3D> generators) {
        // Convert time to natural numbers for the x-axis
        Map<Double, Integer> converter = new HashMap<>();
        MultipleBurstsDataTypes selected =
                MultipleBurstsDataTypes.fromString((String) detailedSpinner.getSelectedItem());
        int count = 1;
        for (int generator = 0; generator < generators.size(); generator++) {
            PlotData3D current;
            if (selected == MultipleBurstsDataTypes.POWER) {
                current = generators.get(generator).getPowerPlotData();
            } else {
                current = generators.get(generator).getPhaseDiffPlotData();
            }

            int startX;
            if (generator == 0 && selected == MultipleBurstsDataTypes.POWER) {
                startX = 0;
            } else {
                startX = 1;
            }

            for (int x = startX; x < current.getXValues().size(); x++) {
                if (!converter.containsKey(current.getXValues().get(x))) {
                    converter.put(current.getXValues().get(x), count);
                    count++;
                }
            }
        }

        List<Datapoint> datapoints = new ArrayList<>();

        for (int generator = 0; generator < generators.size(); generator++) {
            PlotData3D current;

            if (selected == MultipleBurstsDataTypes.POWER) {
                current = generators.get(generator).getPowerPlotData();
            } else {
                current = generators.get(generator).getPhaseDiffPlotData();
            }

            int startX;
            if (generator == 0 && selected == MultipleBurstsDataTypes.POWER) {
                startX = 0;
            } else {
                startX = 1;
            }

            for (int x = startX; x < current.getXValues().size(); x++) {
                for (int y = current.getYValues().size() - 1; y >= 0; y--) {
                    datapoints.add(
                            new Datapoint(
                                    converter.get(current.getXValues().get(x)),
                                    current.getYValues().get(y),
                                    current.getZValues().get(x).get(y)));
                }
            }
        }

        // Convert to datapoints for JSON serialisation later

        return datapoints;
    }

    /**
     * When computing live data we need to keep track of this in order to update the Internal Data
     * Handler correctly.
     *
     * @param bursts - the few bursts that are computed at a time
     */
    @Override
    public void receiveSingleOrManyBursts(List<SingleOrManyBursts> bursts) {
        try {
            // Adding the small list (usually of one) to the current live bursts session
            this.currentLiveBursts.getListOfBursts().addAll(bursts);
        } catch (SingleOrManyBursts.AccessSingleBurstAsManyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionChange(boolean isConnected) {
        this.connected = isConnected;
        if (isConnected) {
            establishConnection();
        } else {
            gatheringData = false;
        }
        // Only change the state if we aren't in the middle of processing data
        if (!InternalDataHandler.getInstance().getProcessingData()) {
            if (dataHasBeenPlottedAtLeastOnce) {
                updateUIState(State.STATIC_COLLECTION_DISPLAYED);
            } else {
                updateUIState(State.INITIAL);
            }
        }
        updateMeasureVisibility();
    }

    @Override
    public void onGatheringDataChange(boolean gatheringData) {
        this.gatheringData = gatheringData;
        updateMeasureVisibility();
        if (!gatheringData) {
            updateUIState(State.STATIC_COLLECTION_DISPLAYED);
        }
    }

    @Override
    public void fileReady(File newFile) {
        simulator = ConnectionSimulator.getInstance();
        if (firstTime) {
            // Create a new directory in groundwater for the incoming data
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-SS");
            String name = "Collection-" + dateFormat.format(new Date());
            idh.setCurrentLiveData(name);
            idh.setProcessingLiveData(true);

            // Add empty cache entry
            cache.put(idh.getCurrentLiveData(), new ArrayList<PlotDataGenerator3D>());

            // Create store for the bursts
            List<SingleOrManyBursts> singles = new ArrayList<>();
            this.currentLiveBursts = new SingleOrManyBursts(singles, null, false);
            idh.silentlySelectCollectionData(this.currentLiveBursts);

            File file = idh.addNewDirectory(idh.getCurrentLiveData());
            currentLiveBursts.setFile(file);
            firstTime = false;
        }

        if (newFile != null) {
            prevAndCurrent.add(newFile);
        }
        // Wait for two files so phase difference can be generated
        if (prevAndCurrent.size() > 1) {
            File previousFile = prevAndCurrent.get(0);
            File currentFile = prevAndCurrent.get(1);

            idh.addFileToDirectory(idh.getCurrentLiveData(), currentFile);
            processLiveData(previousFile, currentFile, !simulator.getDataReady());

            prevAndCurrent.remove(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateMeasureVisibility();
    }
}
