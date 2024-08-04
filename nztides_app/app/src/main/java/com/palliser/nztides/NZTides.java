package com.palliser.nztides;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ScrollView;
import android.widget.TextView;

public class NZTides extends Activity {

    /** Declare variables **/
    //Global strings to hold ports names, etc
    public static final int MENU_ITEM_CHOOSE_PORT = Menu.FIRST;		//Menu item to select ports
    public static final int MENU_ITEM_ABOUT = Menu.FIRST+1;			//Menu item for the about blurb
    public static final String PREFS_NAME = "NZTidesPrefsFile";		//file to store prefs

    private String currentport; 	//String to store the port selected from the menu and prefs file

    // List of the port names for which we have tide data files. Case sensitive, must match the file name!
    final private String[] portlist = {"Akaroa", "AnakakataBay", "Anawhata", "auckland", "BenGunnWharf", "Bluff", "Castlepoint", "Charleston", "Dargaville", "DeepCove", "DogIsland", "Dunedin", "ElaineBay", "ElieBay", "FishingRock-RaoulIsland", "FlourCaskBay", "FreshWaterBasin", "Gisborne", "GreenIsland", "HalfmoonBayOban", "Havelock", "Helensville", "HuruhiHarbour", "JacksonBay", "Kaikoura", "Kaingaroa-ChathamIsland", "Kaiteriteri", "KaitunaRiver", "Kawhia", "KorotitiBay", "Leigh", "LongIsland", "LottinPoint-Wakatiri", "Lyttelton", "ManaMarina", "ManawatuRiverEntrance", "Mano'WarBay", "ManuBay", "Mapua", "MarsdenPoint", "MatiatiaBay", "MotuaraIsland", "MoturikiIsland", "Napier", "Nelson", "NewBrightonPier", "NorthCape-Otou", "Oamaru", "OkukariBay", "OmahaBridge", "Omokoroa", "Onehunga", "Opononi", "OpotikiWharf", "Opua", "Owenga-ChathamIsland", "ParatutaeIsland", "Picton", "PortChalmers", "PortOhopeWharf", "PortTaranaki", "PoutoPoint", "Raglan", "RangatiraPoint", "RangitaikiRiver", "RichmondBay", "Riverton-Aparima", "ScottBase", "SpitWharf", "SumnerHead", "TamakiRiver", "Tarakohe", "Tauranga", "TeWekaBay", "Thames", "Timaru", "TownBasin", "WaihopaiRiverEntrance", "Waitangi-ChathamIsland", "WeitiRiverEntrance", "WelcombeBay", "Wellington", "Westport", "Whakatane", "WhanganuiRiverEntrance", "Whangarei", "Whangaroa", "Whitianga", "WilsonBay"};
    // List of the port names as they will be displayed on screen.
    final private String[] portdisplaynames = {"Akaroa", "Anakakata Bay", "Anawhata", "Auckland", "Ben Gunn Wharf", "Bluff", "Castlepoint", "Charleston", "Dargaville", "Deep Cove", "Dog Island", "Dunedin", "Elaine Bay", "Elie Bay", "Fishing Rock - Raoul Island", "Flour Cask Bay", "Fresh Water Basin", "Gisborne", "Green Island", "Halfmoon Bay - Oban", "Havelock", "Helensville", "Huruhi Harbour", "Jackson Bay", "Kaikoura", "Kaingaroa - ChathamIsland", "Kaiteriteri", "Kaituna River", "Kawhia", "Korotiti Bay", "Leigh", "Long Island", "Lottin Point - Wakatiri", "Lyttelton", "Mana Marina", "Manawatu River Entrance", "Man o'War Bay", "Manu Bay", "Mapua", "Marsden Point", "Matiatia Bay", "Motuara Island", "Moturiki Island", "Napier", "Nelson", "New Brighton Pier", "North Cape - Otou", "Oamaru", "Okukari Bay", "Omaha Bridge", "Omokoroa", "Onehunga", "Opononi", "Opotiki Wharf", "Opua", "Owenga - Chatham Island", "Paratutae Island", "Picton", "Port Chalmers", "Port Ohope Wharf", "Port Taranaki", "Pouto Point", "Raglan", "Rangatira Point", "Rangitaiki River", "Richmond Bay", "Riverton - Aparima", "Scott Base", "Spit Wharf", "Sumner Head", "Tamaki River", "Tarakohe", "Tauranga", "Te Weka Bay", "Thames", "Timaru", "Town Basin", "Waihopai River Entrance", "Waitangi - Chatham Island", "Weiti River Entrance", "Welcombe Bay", "Wellington", "Westport", "Whakatane", "Whanganui River Entrance", "Whangarei", "Whangaroa", "Whitianga", "Wilson Bay"};


	/* private String[] portlist = {
			"auckland",
			"bluff",
			"dunedin",
			"gisborne",
			"lyttelton",
			"marsden point",
			"napier",
			"nelson",
			"onehunga",
			"picton",
			"port chalmers",
			"port taranaki",
			"tauranga",
			"timaru",
			"wellington",
			"westport"
	};

	private String[] portdisplaynames = {
			"Auckland",
			"Bluff",
			"Dunedin",
			"Gisborne",
			"Lyttelton",
			"Marsden Point",
			"Napier",
			"Nelson",
			"Onehunga",
			"Picton",
			"Port Chalmers",
			"Port Taranaki",
			"Tauranga",
			"Timaru",
			"Wellington",
			"Westport"cd
	};
	*/

    //??? Possibly to do with finding the previous/next tide
    public static int swap (int value){
        int b1 = (value >>  0) & 0xff;
        int b2 = (value >>  8) & 0xff;
        int b3 = (value >> 16) & 0xff;
        int b4 = (value >> 24) & 0xff;

        return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
    }

    /** Declare a string to hold the data to be displayed, then populate it **/
    public String calc_outstring(String port){

        AssetManager am = getAssets();		//open the assets folder and find the tide files
        StringBuffer outstring = new StringBuffer("");		//String to hold the text to be displayed on screen

        //declare variables
        int num_rows=8;				//graph height
        int num_cols=34;			//graph width including the vertical line
        int t = 0,told;				//tide times. told = oldest time (time that occurs first) in tide data file
        float h=0;					//Tide height??
        float hold;					//Tide height (oldest tide in the tide data file)
        Date now = new Date();		//Get the current date
        int nowsecs = (int)(now.getTime()/1000);	//Get the current time and format it (not yet sure why divide by 1000)
        int lasttide;								//pointer to the last tide (last one before the file runs out)
        char [][] graph = new char[num_rows][num_cols+1];	//Graph. Array of chars to hold the graph


        /*** Try opening the port.tdat file or catch to display error message if it fails ***/
        try {

            //format variables for display
            DecimalFormat nformat1 = new DecimalFormat(" 0.00;-0.00");	//formatting for tide height in the table section
            DecimalFormat nformat2 = new DecimalFormat("0.00");			//formatting for rate of rise and fall
            DecimalFormat nformat3 = new DecimalFormat("00");			//formatting for minutes ago (tide was xx minutes ago)
            DecimalFormat nformat4 = new DecimalFormat(" 0.0;-0.0");  	//formatting for current height of tide in the header section

            //Format the date and time
            //SimpleDateFormat dformat = new SimpleDateFormat(
            //    	"HH:mm E dd-MM-yyyy zzz");
            SimpleDateFormat dformat = new SimpleDateFormat("HH:mm E dd/MM/yy zzz");


            //Open the tide data file
            DataInputStream tidedat = new DataInputStream(am.open(port+".tdat",1));

			/*Obsolete code. Does nothing. Was probably used to fix some bug in the text formatting
			String stationname_tofu = tidedat.readLine(); 		//stationname with unicode stuff ups
			// byte[] stationnamebytes = stationname_tofu.getBytes(Charset.defaultCharset());
			//String stationname = new String(stationnamebytes, "UTF-8");
			*/

            //read timestamp for last tide in datafile
            lasttide = swap(tidedat.readInt());

            //Read number of records in datafile
            //nrecs = swap(tidedat.readInt()); //Number of records in datafile
            tidedat.readInt(); 		//Read number of records in datafile

            //??
            told = swap(tidedat.readInt());
            hold = (float) (tidedat.readByte())/(float)(10.0);

            //Error message if the oldest time (first time) in the data file is in the future
            if(told>nowsecs){
                outstring.append("The first tide in this datafile doesn't occur until ");
                outstring.append(dformat.format(new Date(1000*(long)told)));
                outstring.append(". The app should start working properly about then.");
            } else {				//the first tide in the data file in is the past so look for the current tide

                //look thru tidedatfile for current time
                for (; ; ) {			//step through the data file checking each tide time
                    t = swap(tidedat.readInt());
                    h = (float) (tidedat.readByte()) / (float) (10.0);		//read the height of tide and divide by 10 (why?)
                    if (t > nowsecs) {		//if the tide time is after the current time then stop processing the file
                        break;
                    }
                    told = t;		//why?
                    hold = h;		//why?
                }

                /* **Calculations for tide, graph, etc. ** */
                //parameters of cosine wave used to interpolate between tides
                //We assume that the tides varies cosinusoidally
                //between the last tide and the next one
                //see NZ Nautical almanac for more details,
                double omega = 2 * Math.PI / ((t - told) * 2);
                double amp = (hold - h) / 2;
                double mn = (h + hold) / 2;
                double x, phase;

                //Populate the array that contains the ascii art plot
                for (int k = 0; k < num_rows; k++) {
                    for (int j = 0; j < num_cols; j++) {
                        graph[k][j] = ' ';
                    }
                    graph[k][num_cols] = '\n';
                }

                for (int k = 0; k < num_cols; k++) {
                    x = (1.0 + (hold > h ? -1 : 1) * Math.sin(k * 2 * Math.PI / (num_cols - 1))) / 2.0;
                    x = ((num_rows - 1) * x + 0.5);
                    graph[(int) x][k] = '*';
                    //graph[k%num_rows][k]='*';
                }

                phase = omega * (nowsecs - told);
                x = (phase + Math.PI / 2) / (2.0 * Math.PI);
                x = ((num_cols - 1) * x + 0.5);
                for (int j = 0; j < num_rows; j++) {
                    graph[j][(int) x] = '|';
                }

                //Calculate the current height of tide and rate of rise or fall
                double currentht = amp * Math.cos(omega * (nowsecs - told)) + mn;			//Current height of tide
                double riserate = -amp * omega * Math.sin(omega * (nowsecs - told)) * 60 * 60;	//rate of rise


                /****** Start populating outstring  *****/
                /** Header section **/
                outstring.append("  " + port + "\n\n");		//Print the port name
                //outstring.append("---------------\n\n");			//Print a separator

                /** Current tide data section **/
                //Print the current height and rate of rise
                outstring.append("Currently:"+ nformat4.format(currentht) + "m ");
                //display up arrow or down arrow depending on weather tide is rising or falling
                if (hold < h)
                    //outstring.append(" \u2191");		//up arrow
                    outstring.append("rising at ");		//up arrow
                else
                    //outstring.append(" \u2193");		//down arrow
                    outstring.append("falling at ");	//down arrow

                outstring.append(nformat2.format(Math.abs(riserate)) + "m/hr\n\n");

                //Calculate the time to the previous or next tide
                int time_to_previous = (nowsecs - told);
                int time_to_next = (t - nowsecs);
                boolean hightidenext = (h > hold);

                if (time_to_previous < time_to_next) {
                    if (hightidenext) {
                        outstring.append("Low tide " + hold + "m was " + (int) (time_to_previous / 3600) +
                                "h " + nformat3.format((int) (time_to_previous / 60) % 60) + "m ago\n");
                    } else {
                        outstring.append("High tide " + hold + "m was " + (int) (time_to_previous / 3600) +
                                "h " + nformat3.format((int) (time_to_previous / 60) % 60) + "m ago\n");
                    }
                } else {
                    if (hightidenext) {
                        outstring.append("High tide " + h + "m in " + (int) (time_to_next / 3600) +
                                "h " + nformat3.format((int) (time_to_next / 60) % 60) + "m\n");
                    } else {
                        outstring.append("Low tide " + h + "m in " + (int) (time_to_next / 3600) +
                                "h " + nformat3.format((int) (time_to_next / 60) % 60) + "m\n");
                    }

                }
                //outstring.append("---------------\n");
                //int num_minutes=(int)((nowsecs-told)/(60));
                //outstring.append("Last tide " + hold + "m,    "+num_minutes/60  + "h" +nformat3.format(num_minutes%60) +"m ago\n");
                //num_minutes=(int)((t -nowsecs)/(60));
                //outstring.append("Next tide " + h +    "m, in "+num_minutes/60  + "h" +nformat3.format(num_minutes%60) +"m\n");
                //outstring.append("---------------\n");
                outstring.append("\n");

                //Graph section
                for (int k = 0; k < num_rows; k++) {
                    for (int j = 0; j < num_cols + 1; j++) {
                        outstring.append(graph[k][j]);
                    }
                }

                //outstring.append("---------------\n");
                outstring.append("\n");


                /*Tides table section*/
                hightidenext = !hightidenext;
                outstring.append(nformat1.format(hold) + (hightidenext ? " H " : " L ") + dformat.format(new Date(1000 * (long) told)) + '\n');
                hightidenext = !hightidenext;
                outstring.append(nformat1.format(h) + (hightidenext ? " H " : " L ") + dformat.format(new Date(1000 * (long) t)) + '\n');

                for (int k = 0; k < 35 * 4; k++) {
                    hightidenext = !hightidenext;
                    t = swap(tidedat.readInt());
                    h = (float) (tidedat.readByte()) / (float) (10.0);
                    outstring.append(nformat1.format(h) + (hightidenext ? " H " : " L ") + dformat.format(new Date(1000 * (long) t)) + '\n');
                }
                outstring.append("The last tide in this datafile occurs at:\n");
                outstring.append(dformat.format(new Date(1000 * (long) lasttide)));
            }

            //Show error message if the tide data file could not be opened for some reason
        }
        catch (IOException e) {
            outstring.append("Problem reading tide data\n\n Try selecting the port again, some times the ports available change with and upgrade. If this doesn't work it is either because the tide data is out of date or you've found some bug, try looking for an update.");
            // outstring.append("\n  Current date is " + now);
            // Printing error
            // outstring.append("\n  Error occured: "
            //	+ e.getMessage()); 	//display a verbose error message for debugging use. This probably doesn't work yet
        }		//end try
        return outstring.toString();		//save the string to the output array
    }	//end of populating output string


    /* ** Initialise the app ** /
    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //restore current port from settings file
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        currentport = settings.getString("CurrentPort","auckland" );

        //setContentView(R.layout.main);
    }


    /* **** Menu **** */
    //ToDo: add menus for "Primary ports", "Secondary ports" and "Ports by region"

    /* ** Create the main menu ** */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // This is our one standard application action -- inserting a
        // new note into the list.
        SubMenu portMenu = menu.addSubMenu(0, MENU_ITEM_CHOOSE_PORT, 0,"Select Port");
        for(int k=0;k<portlist.length;k++)
            portMenu.add(0,Menu.FIRST+10+k,0,portdisplaynames[k]);

        menu.add(0, MENU_ITEM_ABOUT, 0,"About" );

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        // Intent intent = new Intent(null, getIntent().getData());
        // intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        // menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, NotesList.class), null, intent, 0, null);
        return true;
    }

    /* ** Populate the sub menus ** */
    public boolean onOptionsItemSelected (MenuItem item){
        //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        int id = item.getItemId();

        if(id>=Menu.FIRST+10 && id<Menu.FIRST+10+portlist.length){
            currentport = portlist[id-11];
            this.onResume();
            return true;
        }

        switch (id) {
            case MENU_ITEM_ABOUT:
                TextView tv = new TextView(this);
                //tv.setTypeface(Typeface.MONOSPACE);
                tv.setText(R.string.AboutString);//+now.format2445());
                ScrollView sv = new ScrollView(this);
                sv.addView(tv);
                setContentView(sv);
                //quit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /* ** Print outstring to screen after setting the formatting * **/
    @Override
    protected void onResume(){
        String outstring = calc_outstring(currentport);

        //Print the output string to the screen (outstring)
        TextView tv = new TextView(this);
        tv.setTypeface(Typeface.MONOSPACE);
        // Allow textView to scroll
        //tv.setSingleLine(false);
        //tv.setHorizontallyScrolling(true);
        //tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        //tv.setMarqueeRepeatLimit(-1);
        //tv.setSelected(true);
        tv.setPadding(20, 10, 20, 10);
        tv.setTextSize(20);
        //tv.setHorizontallyScrolling(true);

        tv.setText(outstring);		//+now.format2445());

        ScrollView sv = new ScrollView(this);
        sv.addView(tv);
        setContentView(sv);
        super.onResume();
    }


    /* ** Save setting on exit ** */
    @Override
    protected void onStop(){
        super.onStop();

        // Save user preferences. We need an Editor object to
        // make changes. All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("CurrentPort", currentport);

        // Don't forget to commit your edits!!!
        editor.commit();
    }


}
