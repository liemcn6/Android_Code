package com.example.crawldata.SchedulePackage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.crawldata.IP;
import com.example.crawldata.R;

import java.io.DataInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schedule_Fragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Schedule_Fragment() {

    }

    // TODO: Rename and change types and number of parameters
    public static Schedule_Fragment newInstance(String param1, String param2) {
        Schedule_Fragment fragment = new Schedule_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private Socket client;
    private ExpandableListView listView;
    private ArrayList<String> listDataHeader;
    private HashMap<String, List<Schedule>> listDataChild;
    private Custom_Expandable_Schedule adapter;
    private AlertDialog dialogShowSchedule;
    private Spinner spinner;
    private ArrayAdapter adapterSpinner;
    private ProgressDialog loadingBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule_, container, false);

        listDataChild = new HashMap<>();
        listView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        spinner = view.findViewById(R.id.spinner_time);
        spinner.setGravity(Gravity.CENTER);

        addDay(); // Th??m th??? 2,3,4 v??o header c???a Expandble

        // l???y th???i gian v???
        new TimeSchedule().execute(mParam1,mParam2);

        //addTime();

        // B???t s??? ki???n click item
        click_child();

        return view;
    }

    // Click 1 child c???a Expandble
    private void click_child() {
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //Toast.makeText(getContext(), listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).getNameClass(), Toast.LENGTH_SHORT).show();
                showDialog(listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition));
                return false;
            }
        });
    }

    // Hi???n th??? chi ti???t m???t m??n h???c khi click v??o
    private void showDialog(Schedule schedule){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_schedule,
                (ViewGroup) getActivity().findViewById(R.id.layoutShowSchedule), false);
        builder.setView(view);
        dialogShowSchedule = builder.create();
        if(dialogShowSchedule.getWindow() != null){
            dialogShowSchedule.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        TextView txtName, txtRoom, txtKip, txtNhom, txtGv;
        txtName = view.findViewById(R.id.nameSubject);
        txtRoom = view.findViewById(R.id.roomSubject);
        txtKip = view.findViewById(R.id.kipSubject);
        txtNhom = view.findViewById(R.id.nhomSubject);
        txtGv = view.findViewById(R.id.gvSubject);

        txtName.setText("M??n : " + schedule.getNameClass());
        txtRoom.setText("Ph??ng : " + schedule.getRoom());
        txtKip.setText("K??p : " + schedule.getTimeOfDay());
        txtNhom.setText("M?? : " + schedule.getGroup());
        txtGv.setText("Gi???ng vi??n : " + schedule.getLecturer());


        dialogShowSchedule.show();

    }

    // Th??m th??? 2,3,4 v??o header c???a Expandble
    private void addDay(){
        listDataHeader = new ArrayList<String>();
        listDataHeader.add("Th??? Hai");
        listDataHeader.add("Th??? Ba");
        listDataHeader.add("Th??? T??");
        listDataHeader.add("Th??? N??m");
        listDataHeader.add("Th??? S??u");
        listDataHeader.add("Th??? B???y");
    }


    // X??? l?? th???i gian, ki???m tra xem ??ang ch???n xem th???i kh??a bi???u c???a tu???n n??o
    private int checkTime(ArrayList<String>listTime){
        int pos = 0;
        boolean check = false;
        for (int i = 0; i < listTime.size(); i++){
            String time = listTime.get(i);
            String regex = "[0-3][0-9]\\/[0-1][0-9]\\/[0-9]{4}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(time.toString());

            ArrayList<String> times = new ArrayList<>();
            while (matcher.find()) {
                times.add(matcher.group(0));
            }

            try {
                Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(times.get(0).toString());
                Date date2 = new SimpleDateFormat("dd/MM/yyyy").parse(times.get(1).toString());
                Date date = new Date();

                if (date.before(date2) && date.after(date1)){
                    pos = i;
                    check = true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (check) break;
        }
        return pos;
    }

    // Nh???n d??? li???u th???i kh??a bi???u
    public class ScheduleData extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            String result = "No data";

            String user = strings[0];
            String pass = strings[1];
            String time = strings[2];
            String type = strings[3];
            try {
                client = new Socket(IP.ip,60000);

                PrintWriter pw = new PrintWriter(client.getOutputStream(),true);
                pw.println("schedule" + ";" + user + ";" + pass + ";" + time + ";" + type);


                DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
                result = dataInputStream.readUTF();

                client.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            List<Schedule> schedules = new ArrayList<>();
            if (s.equals("No data")){
                Toast.makeText(getContext(), "Kh??ng c?? d??? li???u", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }else{
                StringTokenizer st = new StringTokenizer(s, ","); // t??ch th??nh t???ng m??n m???t

                while (st.hasMoreTokens()){
                    String rs = st.nextToken();
                    StringTokenizer sc = new StringTokenizer(rs, ":");
                    int i = 0; // ki???m tra xem ??ang l???y v??? tr?? n??o c???a token
                    Schedule schedule = new Schedule();

                    while (sc.hasMoreTokens()){
                        if(i == 0){
                            schedule.setNameClass(sc.nextToken());
                        }
                        else if(i == 1){
                            schedule.setLecturer(sc.nextToken());
                        }
                        else if(i == 2){
                            schedule.setRoom(sc.nextToken());
                        }
                        else if(i == 3){
                            schedule.setTimeOfDay(sc.nextToken());
                        }
                        else if(i == 4){
                            schedule.setDayOfWeek(sc.nextToken());
                        }
                        else if(i == 5){
                            schedule.setGroup(sc.nextToken());
                        }
                        i++;
                    }
                    schedules.add(schedule);
                }

                ArrayList<Schedule> scT2 = new ArrayList<>();
                ArrayList<Schedule> scT3 = new ArrayList<>();
                ArrayList<Schedule> scT4 = new ArrayList<>();
                ArrayList<Schedule> scT5 = new ArrayList<>();
                ArrayList<Schedule> scT6 = new ArrayList<>();
                ArrayList<Schedule> scT7 = new ArrayList<>();

                for (int i = 0; i < schedules.size(); i++){
                    if (schedules.get(i).getDayOfWeek().equals("Th??? Hai")){
                        scT2.add(schedules.get(i));
                    }

                    if (schedules.get(i).getDayOfWeek().equals("Th??? Ba")){
                        scT3.add(schedules.get(i));
                    }

                    if (schedules.get(i).getDayOfWeek().equals("Th??? T??")){
                        scT4.add(schedules.get(i));
                    }

                    if (schedules.get(i).getDayOfWeek().equals("Th??? N??m")){
                        scT5.add(schedules.get(i));
                    }

                    if (schedules.get(i).getDayOfWeek().equals("Th??? S??u")){
                        scT6.add(schedules.get(i));
                    }

                    if (schedules.get(i).getDayOfWeek().equals("Th??? B???y")){
                        scT7.add(schedules.get(i));
                    }
                }

                listDataChild.put(listDataHeader.get(0),scT2);
                listDataChild.put(listDataHeader.get(1),scT3);
                listDataChild.put(listDataHeader.get(2),scT4);
                listDataChild.put(listDataHeader.get(3),scT5);
                listDataChild.put(listDataHeader.get(4),scT6);
                listDataChild.put(listDataHeader.get(5),scT7);

                adapter = new Custom_Expandable_Schedule(getContext(),listDataHeader, listDataChild);
                listView.setAdapter(adapter);

                loadingBar.dismiss();
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    // Nh???n d??? li???u v??? th???i gian "Tu???n 01 [T???.....]"
    public class TimeSchedule extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "No data";

            String user = strings[0];
            String pass = strings[1];

            try {
                client = new Socket(IP.ip, 60000);

                PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
                pw.println("time" + ";" + user + ";" + pass);

                DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
                result = dataInputStream.readUTF();

                client.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("No data")) {
                Toast.makeText(getContext(), "Kh??ng c?? d??? li???u", Toast.LENGTH_SHORT).show();
                //loadingBar.dismiss();
            } else {
                StringTokenizer st = new StringTokenizer(s, ",");
                final ArrayList<String> times = new ArrayList<>(); // m???ng l??u tr??? th???i gian (tu???n 1.....)
                while (st.hasMoreTokens()){
                    times.add(st.nextToken());
                }

                adapterSpinner = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,times);
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // T???o kho???ng c??ch gi???a c??c item
                adapterSpinner.setNotifyOnChange(true);
                spinner.setAdapter(adapterSpinner);

                final int position = checkTime(times);
                spinner.setSelection(position);

                // b???t s??? ki???n click ch???n 1 tu???n tr??n spinner
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        loadingBar = new ProgressDialog(getContext());
                        loadingBar.setTitle("??ang l???y d??? li???u...");
                        loadingBar.setMessage("Ch??? trong gi??y l??t..");
                        loadingBar.setCanceledOnTouchOutside(true);
                        loadingBar.show();

                        if (pos == position) {
                            new ScheduleData().execute(mParam1,mParam2,times.get(pos),"1");
                        }
                        else{
                            new ScheduleData().execute(mParam1, mParam2,times.get(pos),"2");
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    // add time th??? c??ng - ???? b???
    private void addTime(ArrayList<String> times) {
        ArrayList listTime = new ArrayList<>();
        listTime.add("Tu???n 01 [T??? 07/09/2020 -- ?????n 13/09/2020]");
        listTime.add("Tu???n 02 [T??? 14/09/2020 -- ?????n 20/09/2020]");
        listTime.add("Tu???n 03 [T??? 07/09/2020 -- ?????n 27/09/2020]");
        listTime.add("Tu???n 04 [T??? 28/09/2020 -- ?????n 04/10/2020]");
        listTime.add("Tu???n 05 [T??? 05/10/2020 -- ?????n 11/10/2020]");
        listTime.add("Tu???n 06 [T??? 12/10/2020 -- ?????n 18/10/2020]");
        listTime.add("Tu???n 07 [T??? 19/10/2020 -- ?????n 25/10/2020]");
        listTime.add("Tu???n 08 [T??? 26/10/2020 -- ?????n 01/11/2020]");
        listTime.add("Tu???n 09 [T??? 02/11/2020 -- ?????n 08/11/2020]");
        listTime.add("Tu???n 10 [T??? 09/11/2020 -- ?????n 15/11/2020]");
        listTime.add("Tu???n 11 [T??? 16/11/2020 -- ?????n 22/11/2020]");
        listTime.add("Tu???n 12 [T??? 23/11/2020 -- ?????n 29/11/2020]");
        listTime.add("Tu???n 13 [T??? 30/11/2020 -- ?????n 06/12/2020]");
        listTime.add("Tu???n 14 [T??? 07/12/2020 -- ?????n 13/12/2020]");
        listTime.add("Tu???n 15 [T??? 14/12/2020 -- ?????n 20/12/2020]");
        listTime.add("Tu???n 16 [T??? 21/12/2020 -- ?????n 27/12/2020]");
        listTime.add("Tu???n 17 [T??? 28/12/2020 -- ?????n 03/01/2021]");

    }
}