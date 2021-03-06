package app.calcounter.com.projectversion1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.calcounter.com.projectversion1.Model.LocationListItem;

public class TaskViewer extends AppCompatActivity {

    public static final String LOCATION_COUNTER_FB = "LocationCounterFB";
    //Recycler
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    AlertDialog.Builder builder;
    //
    private List<ListItem> listItems;
    private int counter = 0;
    private String m_TextTask = "";
    private String m_TextPriority = "";
    private int taskCounter = 0;
    private int taskCounterForAdapter = 0;
    public static final String LAST_DOCUMENT_ID = "LastDocumentID";

    public static final String ADDRESS = "address";
    public static final String ADDRESS_2 = "address2";
    public static final String CITY = "city";
    public static final String STATE = "state";
    public static final String ZIP = "zip";

    private Button returnbtn;
    int viewPosition =0;


    // added
    private DocumentReference mDocRef;
    private DocumentReference mLocalTotal = FirebaseFirestore.getInstance().collection("appSetup").document("initFile");
    FirebaseFirestore db = FirebaseFirestore.getInstance();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_viewer);
       // getActionBar().setTitle("Tasks");
        getSupportActionBar().setTitle("Tasks");
        recyclerView = (RecyclerView) findViewById(R.id.recyclerviewdre);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listItems = new ArrayList<>();

        returnbtn = (Button) findViewById(R.id.returnbtnTaskViewer);

        // returns to Locations activity
        // returns to Locations activity
        // returns to Locations activity

        int loc = getIntent().getExtras().getInt("locCounter"); // if this works
        // you can query all tasks that match locCounter



        //Query query = db.collection("tasks").whereEqualTo("rootloc", "loc"+loc);
        // not sure how to read this vs iterating over the collection


        Intent previous = getIntent(); // for previous data
        boolean isView = previous.getExtras().getBoolean("isView");
        viewPosition = previous.getExtras().getInt("positonFromLocationAdapter");
        Log.e("this is the adapter position", Integer.toString(viewPosition));

        taskCounterForAdapter = 0; // this reset is not needed but it is needed with rotations and the fb
        // keep with the following code for simplicity


        if(isView) // view only version
        {
            db.collection("tasks").whereEqualTo("rootloc", "loc" + viewPosition) // will iterate over the collection
                    .get() // this listener should be safe for activity change
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                for(QueryDocumentSnapshot document : task.getResult())
                                {
                                    if(document.getData() != null) // prevents crashes with null data does not prevent crash from singlular null reading
                                    {
                                        taskCounterForAdapter++;

                                        //dataToSave.put("priority",temp);
                                        // need data reads here number of task that equal location
                                        Map<String,Object> dataToRead = document.getData();
                                        String nextPriority = dataToRead.get("priority").toString();
                                        Log.e("priority     a>>>>", nextPriority);



                                        ListItem item = new ListItem("Task" + taskCounterForAdapter, "priority " + dataToRead.get("priority").toString(), Integer.parseInt(nextPriority));
                                        // ListItem item = new ListItem("Task50", "booob", 10);
                                        listItems.add(item);
                                        setAdapterValues();
                                        prioritize();
                                        Log.e("it worked like a charm", document.getId() + " => " + document.getData());
                                    }

                                }

                            }else
                            {
                                Log.e("Firebase blows", "error", task.getException());
                            }

                            //setAdapterValues();
                        }
                    });
        }
        else
        {
            // this means from location create task
            //mbundle.putInt("locCounter",x);
            int tasknum = getIntent().getExtras().getInt("locCounter");



            db.collection("tasks").whereEqualTo("rootloc", "loc" + tasknum) // will iterate over the collection
                    .get() // this listener should be safe for activity change
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                for(QueryDocumentSnapshot document : task.getResult())
                                {

                                    if(document.getData() != null) // prevents crashes with totally null data, but not for single fields
                                    {
                                        taskCounterForAdapter++;
                                        //

                                        //dataToSave.put("priority",temp);
                                        // need data reads here number of task that equal location
                                        Map<String,Object> dataToRead = document.getData();
                                        String nextPriority = dataToRead.get("priority").toString();
                                        Log.e("priority     a>>>>", nextPriority);
                                        ListItem item = new ListItem("Task" + taskCounterForAdapter, "priority " + dataToRead.get("priority").toString(), Integer.parseInt(nextPriority));

                                        listItems.add(item);
                                        setAdapterValues(); //abstracted some of the calls to reload recycler
                                        prioritize(); // sorts it
                                        Log.e("it worked like a charm", document.getId() + " => " + document.getData());
                                    }

                                }

                            }else
                            {
                                Log.e("Firebase blows", "error", task.getException());
                            }

                            //setAdapterValues();
                        }
                    });
        }

        //boolean isView = (boolean) getIntent().getExtras().get("isView");
        //intent.putExtra("positonFromLocationAdapter", position);
        //intent.putExtra("isView", true);


        // move into a function
        //db.collection("tasks").



        // move into a function
        returnbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent(TaskViewer.this, AdminLocationsActivity.class);

                final boolean isView = getIntent().getExtras().getBoolean("isView");

                // if this is removed it will add nulls into the database
                if(isView) // shortcircuit back without bad data
                {
                    Log.e("the is a view feature worked", "worked worked worked");
                    finish();
                    startActivity(returnIntent);
                }

                if(!isView)
                {
                    Log.e("the is a view feature didnt work", "this is fucked");
                    String addressLineOne = getIntent().getExtras().getString(ADDRESS);
                    String addressLineTwo = getIntent().getExtras().getString(ADDRESS_2);
                    String city = getIntent().getExtras().getString(CITY);
                    String state = getIntent().getExtras().getString(STATE);
                    String zip = getIntent().getExtras().getString(ZIP);
                    String locationDocID = getIntent().getExtras().getString(LAST_DOCUMENT_ID);
                    String[] rooms = new String[listItems.size()];
                    for (int i = 0; i < rooms.length; i++){
                        rooms[i] = listItems.get(i).getName();
                    }
                    int locationCounter = getIntent().getExtras().getInt("locCounter");// keep track out outside
                    locationCounter++;


                    //old code
                    //old code
                    //old code

                    //mDocRef = FirebaseFirestore.getInstance().collection("locations").document("loc" + locationCounter);

                    if(locationDocID == null)
                    {
                        locationDocID = "loc0";
                    }

                    if(locationDocID.length() == 4)
                    {
                        locationDocID = locationDocID.substring(3);
                        Log.e("test",locationDocID);
                    }

                    if(locationDocID.length() == 5)
                    {
                        locationDocID = locationDocID.substring(3,5);
                        Log.e("test",locationDocID);
                    }

                    if(locationDocID.length() == 6)
                    {
                        locationDocID = locationDocID.substring(3,6);
                        Log.e("test",locationDocID);
                    }

                    Log.e("this is the string value", locationDocID);
                    int temp = Integer.parseInt(locationDocID);
                    temp++;


                    mDocRef = FirebaseFirestore.getInstance().collection("locations").document("loc" + temp); // this is promoted to string



                    Map<String,Object> dataToSave = new HashMap<String,Object>();

                    // don't need to add counter because it is save on doc name?
                    dataToSave.put(ADDRESS,addressLineOne);
                    dataToSave.put(ADDRESS_2,addressLineTwo);
                    dataToSave.put(CITY,city);
                    dataToSave.put(STATE,state);
                    dataToSave.put(ZIP,zip);

                    // need to retrive this value outside the listener



                    mDocRef.set((dataToSave)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // this means the location saved to firebase and thus the counter needs to be increased
                            // so the project does not overwrite a existing location after deletions


                            // this is to store it as a hashmap can it just be stored as the boxed type?
                            String localLocationCounter = "";
                            mLocalTotal.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists())
                                    {
                                        String localLocationCounter = documentSnapshot.getString(LOCATION_COUNTER_FB);
                                    }
                                    else
                                    {
                                        String localLocationCounter = "0";
                                    }
                                }
                            });
                            int temp =0;
                            try{
                                temp = Integer.parseInt(localLocationCounter);
                            }catch(Exception e)
                            {

                            }
                            temp++;
                            // this is overly complicated to store as a hashmap
                            Map<String,Object> locationCounterOnFireBase = new HashMap<String, Object>();
                            locationCounterOnFireBase.put(LOCATION_COUNTER_FB,Integer.toString(temp));

                            mLocalTotal.set(locationCounterOnFireBase).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // actually puts the value up
                                }
                            });


                            Log.d("Location saved", "Location saved");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Loc didn't save", "didn't save loc");
                        }
                    });



                    finish();
                    startActivity(returnIntent);
                }


            }
        });


//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);





//        for(int i = 0; i < 10; i++) {
//            counter++;
//        ListItem item = new ListItem("Task " + (counter + 1), "Priority", counter%5);
//
//        listItems.add(item);
//    }
//
//      setAdapterValues();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                builder = new AlertDialog.Builder(TaskViewer.this);
                builder.setTitle("Enter Task Name & priority");

                Context context = TaskViewer.this;
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);


                final EditText inputTask = new EditText(context);
                inputTask.setHint("Task Name");
                layout.addView(inputTask);


                final EditText inputPriority = new EditText(context);
                inputPriority.setHint("Priority 1-5");
                layout.addView(inputPriority);

                builder.setView(layout);

                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_TextTask = inputTask.getText().toString();
                        m_TextPriority = inputPriority.getText().toString();
                        int temp =0;
                        boolean pass = false;

                        try{
                            temp = Integer.parseInt(m_TextPriority);
                        }
                        catch(Exception e) {
                            dialog.cancel();
                        };

                        if(temp < 0 || temp > 6)
                        {
                            Context context = getApplicationContext();
                            CharSequence text = "Priority out of range";
                            int duration = Toast.LENGTH_LONG;
                            Toast toast = Toast.makeText(context,text,duration);
                            toast.show();
                            dialog.cancel();
                        }
                        else
                        {
                            pass = true;
                        }

                        if(m_TextTask != "" && pass == true)
                        {
                            pass = false;
                            if(m_TextTask.length() > 15)
                            {
                                m_TextTask = m_TextTask.substring(0,15);
                            }
                            int locationCounter = getIntent().getExtras().getInt("locCounter");



                            // need to clear view first or items will double up
                            // need to clear view first or items will double up
                            // need to clear view first or items will double up
                            listItems.clear();
                            recyclerView.removeAllViewsInLayout();

                            //change all tasks to display priority
                            //change all tasks to display priority
                            taskCounterForAdapter =0; // this reset is needed

                            if(isView) // view only version
                            {

                                db.collection("tasks").whereEqualTo("rootloc", "loc" + viewPosition) // will iterate over the collection
                                        .get() // this listener should be safe for activity change
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful())
                                                {
                                                    for(QueryDocumentSnapshot document : task.getResult())
                                                    {
                                                        if(document.getData() != null) // prevents crashes with null data does not prevent crash from singlular null reading
                                                        {
                                                            taskCounterForAdapter++;

                                                            //dataToSave.put("priority",temp);
                                                            // need data reads here number of task that equal location
                                                            Map<String,Object> dataToRead = document.getData();
                                                            String nextPriority = dataToRead.get("priority").toString();
                                                            Log.e("priority     a>>>>", nextPriority);



                                                            ListItem item = new ListItem("Task" + taskCounterForAdapter, dataToRead.get("priority").toString(), Integer.parseInt(nextPriority));
                                                            // ListItem item = new ListItem("Task50", "booob", 10);
                                                            listItems.add(item);
                                                            setAdapterValues();
                                                            prioritize();
                                                            Log.e("it worked like a charm", document.getId() + " => " + document.getData());
                                                        }

                                                    }

                                                }else
                                                {
                                                    Log.e("Firebase blows", "error", task.getException());
                                                }

                                                //setAdapterValues();
                                            }
                                        });
                            }
                            else
                            {
                                // this means from location create task
                                //mbundle.putInt("locCounter",x);
                                int tasknum = getIntent().getExtras().getInt("locCounter");


                                db.collection("tasks").whereEqualTo("rootloc", "loc" + tasknum) // will iterate over the collection
                                        .get() // this listener should be safe for activity change
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful())
                                                {
                                                    for(QueryDocumentSnapshot document : task.getResult())
                                                    {

                                                        if(document.getData() != null) // prevents crashes with totally null data, but not for single fields
                                                        {
                                                            taskCounterForAdapter++;
                                                            //

                                                            //dataToSave.put("priority",temp);
                                                            // need data reads here number of task that equal location
                                                            Map<String,Object> dataToRead = document.getData();
                                                            String nextPriority = dataToRead.get("priority").toString();
                                                            Log.e("priority     a>>>>", nextPriority);
                                                            ListItem item = new ListItem("Task" + taskCounterForAdapter, "priority" + dataToRead.get("priority").toString(), Integer.parseInt(nextPriority));

                                                            listItems.add(item);
                                                            setAdapterValues(); //abstracted some of the calls to reload recycler
                                                            prioritize(); // sorts it
                                                            Log.e("it worked like a charm", document.getId() + " => " + document.getData());
                                                        }

                                                    }

                                                }else
                                                {
                                                    Log.e("Firebase blows", "error", task.getException());
                                                }

                                                //setAdapterValues();
                                            }
                                        });
                            }




//                            db.collection("tasks") // will iterate over the collection
//                                    .get() // this listener should be safe for activity change
//                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                            if(task.isSuccessful())
//                                            {
//                                                for(QueryDocumentSnapshot document : task.getResult())
//                                                {
//                                                    taskCounter++; // increments tasks so they won't overwrite each other
//
//
//                                                }
//
//                                            }else
//                                            {
//                                                Log.e("Firebase blows", "error", task.getException());
//                                            }
//                                        }
//                                    });

                            // working on right now
                            // working on right now
                            // working on right now


                            // need to add data to firebase here
                            // needs to have a field for the location it belongs to
                            // that is not done yet

                            // need to read existing tasks also so we don't overwrite
                            // need task number

                            ++taskCounter;
                            mDocRef = FirebaseFirestore.getInstance().collection("tasks").document("task" + (taskCounter +1));
                            Map<String,Object> dataToSave = new HashMap<String,Object>();

                            dataToSave.put("taskname",m_TextTask); // puts name string
                            dataToSave.put("priority",temp); // the priority number, already validated
                            dataToSave.put("rootloc","loc" + locationCounter);

                            mDocRef.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.e("Task saved", "task saved");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Task didn't save", "didn't save");
                                }
                            });

                            // read data into the recyclerviewer
                            // read data into the recyclerviewer
                            // read data into the recyclerviewer
                            // read data into the recyclerviewer

                            counter++; //TODo not sure what this is for cleanup?


                            // this should not be here
                            // this should not be here
                            // this should not be here


                            //ListItem item = new ListItem(m_TextTask, "Priority ", temp);
                            //listItems.add(item);
                            //setAdapterValues();
                            //prioritize();
                        }
                        else
                        {
                            dialog.cancel();
                        }


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();



            }
        });
    }

    void setAdapterValues ()
    {
        adapter = new TaskAdapter(this,listItems); // object not the class
        recyclerView.setAdapter(adapter);
    }

    void prioritize()
    {

        listItems.sort(new Comparator<ListItem>() {
            @Override
            public int compare(ListItem t1, ListItem t2) {

                if(t1.getPriority() == t2.getPriority())
                {
                    return 0;
                }

                if(t1.getPriority() > t2.getPriority())
                {
                    return 1;
                }
                else
                {
                    return -1;
                }
            }
        });

        setAdapterValues();

    }

}
