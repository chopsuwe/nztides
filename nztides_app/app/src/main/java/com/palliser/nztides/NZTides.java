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
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ScrollView;
import android.widget.TextView;

public class NZTides extends Activity {

	/** Declare variables **/
	//Global strings to hold ports names, etc
    public static final int MENU_ITEM_CHOOSE_PORT = Menu.FIRST;		//Fist menu item...select ports
    public static final int MENU_ITEM_ABOUT = Menu.FIRST+1;			//Second menu item...the About blurb
    public static final String PREFS_NAME = "NZTidesPrefsFile";		//file to store user preferences


    private String currentport; 	//String to store the port selected from the menu and prefs file

	// List of the port names for which we have tide data files. Case sensitive, must match the file name!
	final private String[] portlist = {"Akaroa", "AnakakataBay", "Anawhata", "Auckland", "BenGunnWharf", "Bluff", "Castlepoint", "Charleston", "Dargaville", "DeepCove", "DogIsland", "Dunedin", "ElaineBay", "ElieBay", "FishingRock-RaoulIsland", "FlourCaskBay", "FreshWaterBasin", "Gisborne", "GreenIsland", "HalfmoonBayOban", "Havelock", "Helensville", "HuruhiHarbour", "JacksonBay", "Kaikoura", "Kaingaroa-ChathamIsland", "Kaiteriteri", "KaitunaRiver", "Kawhia", "KorotitiBay", "Leigh", "LongIsland", "LottinPoint-Wakatiri", "Lyttelton", "ManaMarina", "ManawatuRiverEntrance", "Mano'WarBay", "ManuBay", "Mapua", "MarsdenPoint", "MatiatiaBay", "MotuaraIsland", "MoturikiIsland", "Napier", "Nelson", "NewBrightonPier", "NorthCape-Otou", "Oamaru", "OkukariBay", "OmahaBridge", "Omokoroa", "Onehunga", "Opononi", "OpotikiWharf", "Opua", "Owenga-ChathamIsland", "ParatutaeIsland", "Picton", "PortChalmers", "PortOhopeWharf", "PortTaranaki", "PoutoPoint", "Raglan", "RangatiraPoint", "RangitaikiRiver", "RichmondBay", "Riverton-Aparima", "ScottBase", "SpitWharf", "SumnerHead", "TamakiRiver", "Tarakohe", "Tauranga", "TeWekaBay", "Thames", "Timaru", "TownBasin", "WaihopaiRiverEntrance", "Waitangi-ChathamIsland", "WeitiRiverEntrance", "WelcombeBay", "Wellington", "Westport", "Whakatane", "WhanganuiRiverEntrance", "Whangarei", "Whangaroa", "Whitianga", "WilsonBay"};
	// List of the port names as they will be displayed on screen.
	final private String[] portdisplaynames = {"Akaroa", "Anakakata Bay", "Anawhata", "+ Auckland", "Ben Gunn Wharf", "+ Bluff", "Castlepoint", "Charleston", "Dargaville", "Deep Cove", "Dog Island", "+ Dunedin", "Elaine Bay", "Elie Bay", "Fishing Rock - Raoul Island", "Flour Cask Bay", "Fresh Water Basin", "+ Gisborne", "Green Island", "Halfmoon Bay - Oban", "Havelock", "Helensville", "Huruhi Harbour", "Jackson Bay", "Kaikoura", "Kaingaroa - Chatham Island", "Kaiteriteri", "Kaituna River", "Kawhia", "Korotiti Bay", "Leigh", "Long Island", "Lottin Point - Wakatiri", "+ Lyttelton", "Mana Marina", "Manawatū River Entrance", "Man o'War Bay", "Manu Bay", "Mapua", "+ Marsden Point", "Matiatia Bay", "Motuara Island", "Moturiki Island", "+ Napier", "+ Nelson", "New Brighton Pier", "North Cape - Otou", "Oamaru", "Okukari Bay", "Omaha Bridge", "Omokoroa", "+ Onehunga", "Opononi", "Opotiki Wharf", "Opua", "Owenga - Chatham Island", "Paratutae Island", "+ Picton", "+ Port Chalmers", "Port Ohope Wharf", "+ Port Taranaki", "Pouto Point", "Raglan", "Rangatira Point", "Rangitaiki River", "Richmond Bay", "Riverton - Aparima", "Scott Base", "Spit Wharf", "Sumner Head", "Tamaki River", "Tarakohe", "+ Tauranga", "Te Weka Bay", "Thames", "+ Timaru", "Town Basin", "Waihopai River Entrance", "Waitangi - Chatham Island", "Weiti River Entrance", "Welcombe Bay", "+ Wellington", "+ Westport", "Whakatane", "Whanganui River Entrance", "Whangarei", "Whangaroa", "Whitianga", "Wilson Bay"};


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

	//?Unknown? Probably to do with finding the previous/next tide or daylight savings
	public static int swap (int value) 
	{
		int b1 = (value >>  0) & 0xff;   //bitshift value 0 places right then bitwise AND with FF
		int b2 = (value >>  8) & 0xff;
		int b3 = (value >> 16) & 0xff;
		int b4 = (value >> 24) & 0xff;

		return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;  //bitshift them all back to the right. Why? Dunno.
	}

	/** Declare a string to hold the data to be displayed, then populate it **/
	public String calc_outstring(String port){
	       
		AssetManager am = getAssets();		//open the assets folder containing the tide files
		StringBuffer outstring =  new StringBuffer("");		//String to hold the text to be displayed on screen

		//declare variables
		int num_rows=8;				//graph height
		int num_cols=34;			//graph width including the vertical line
		int t = 0;					//Time of next tide
		int told;					//Time of previous tide
		float h=0;					//Height of next tide
		float hold;					//Height of previous tide
		Date now = new Date();		//Get the current date  //new keyword allocates memory
		int nowsecs = (int)(now.getTime()/1000);	//Get the current time and format it (not yet sure why divide by 1000)
		int lasttide;								//pointer to the last tide (last one before the file runs out)
		char [][] graph = new char[num_rows][num_cols+1];	//Graph. Array of chars to hold the graph
		


		/*** Try opening the port.tdat file or catch to display error message if it fails ***/
	    try {

			//format variables for display
			DecimalFormat nformat1 = new DecimalFormat(" 0.0;-0.0");	//formatting for tide height in the table section
			DecimalFormat nformat2 = new DecimalFormat("0.00");			//formatting for rate of rise and fall
			DecimalFormat nformat3 = new DecimalFormat("00");			//formatting for minutes ago (tide was xx minutes ago)
			DecimalFormat nformat4 = new DecimalFormat(" 0.0;-0.0");  	//formatting for current height of tide in the header section

			//Format the date and time
			//SimpleDateFormat dformat = new SimpleDateFormat(
			//    	"HH:mm E dd-MM-yyyy zzz");           //Date format for table section "16:47 Thu 29/08/24 GMT+12:00"
			SimpleDateFormat dformat = new SimpleDateFormat("HH:mm E dd/MM/yy zzz");


			//Open the tide data file
	    	DataInputStream tidedat = new DataInputStream(am.open(port+".tdat",1));

			/*Obsolete code. Read the port name from inside the .tdat file
			String stationname_tofu = tidedat.readLine(); 		//stationname with unicode stuff ups
			// byte[] stationnamebytes = stationname_tofu.getBytes(Charset.defaultCharset());
			//String stationname = new String(stationnamebytes, "UTF-8");
			*/

			//read timestamp for last tide in datafile
	    	lasttide = swap(tidedat.readInt());

			//Read number of records in datafile
	    	//nrecs = swap(tidedat.readInt()); //Number of records in datafile
			tidedat.readInt(); 		//Read number of records in datafile

			//?? Unknown as yet ?
	    	told = swap(tidedat.readInt());
	        hold = (float) (tidedat.readByte())/(float)(10.0);

			//Error message if the oldest time (first time) in the data file is in the future
			if(told>nowsecs){
				outstring.append("The first tide in this datafile doesn't occur until ");
				outstring.append(dformat.format(new Date(1000*(long)told)));
				outstring.append(". The app should start working properly about then.");
			} else {				//the first tide in the data file in is the past so look for the current tide

				//look thru tidedatfile for current time
				for (; ; ) {
					t = swap(tidedat.readInt());			//step through the data file checking each tide time
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
						graph[k][j] = ' ';				//Populate spaces
					}
					graph[k][num_cols] = '\n';
				}

				for (int k = 0; k < num_cols; k++) {
					x = (1.0 + (hold > h ? -1 : 1) * Math.sin(k * 2 * Math.PI / (num_cols - 1))) / 2.0;
					x = ((num_rows - 1) * x + 0.5);
					graph[(int) x][k] = '*';			//Populate stars
					//graph[k%num_rows][k]='*';
				}

				phase = omega * (nowsecs - told);
				x = (phase + Math.PI / 2) / (2.0 * Math.PI);
				x = ((num_cols - 1) * x + 0.5);
				for (int j = 0; j < num_rows; j++) {
					graph[j][(int) x] = '|';			//Populate the current time marker line
				}

				//Calculate the current height of tide and rate of rise or fall
				double currentht = amp * Math.cos(omega * (nowsecs - told)) + mn;			//Current height of tide
				double riserate = -amp * omega * Math.sin(omega * (nowsecs - told)) * 60 * 60;	//rate of rise


				/****** Start populating outstring  *****/
				/** Header section **/
				outstring.append("\n       " + port + "\n\n");		//Print the port name
				outstring.append("---------------\n\n");			//Print a separator line

				/** Current tide data section **/
				//Print the current height and rate of rise
				outstring.append(" Currently:"+ nformat4.format(currentht) + "m ");
				//display up arrow or down arrow depending on weather tide is rising or falling
				if (hold < h) {
					//outstring.append(" \u2191");		//up arrow
					outstring.append("rising at ");		//up arrow
				} else {
					//outstring.append(" \u2193");		//down arrow
					outstring.append("falling at ");	//down arrow
					

				outstring.append(nformat2.format(Math.abs(riserate)) + "m/hr\n\n");		//Display the rate of rise or fall

				//Calculate the time to the previous or next tide
				int time_to_previous = (nowsecs - told);
				int time_to_next = (t - nowsecs);
				boolean hightidenext = (h > hold);

				if (time_to_previous < time_to_next) {
					if (hightidenext) {			//Display the previous low tide
						outstring.append(" Low tide " + hold + "m was " + (int) (time_to_previous / 3600) +
								"h " + nformat3.format((int) (time_to_previous / 60) % 60) + "m ago\n");
					} else {					//Display the previous high tide
						outstring.append(" High tide " + hold + "m was " + (int) (time_to_previous / 3600) +
								"h " + nformat3.format((int) (time_to_previous / 60) % 60) + "m ago\n");
					}
				} else {
					if (hightidenext) {			//Display the next high tide
						outstring.append(" High tide " + h + "m in " + (int) (time_to_next / 3600) +
								"h " + nformat3.format((int) (time_to_next / 60) % 60) + "m\n");
					} else {					//Display the next low tide
						outstring.append(" Low tide " + h + "m in " + (int) (time_to_next / 3600) +
								"h " + nformat3.format((int) (time_to_next / 60) % 60) + "m\n");
					}

				}
				//outstring.append("---------------\n");
				//int num_minutes=(int)((nowsecs-told)/(60));
				//outstring.append("Last tide " + hold + "m,    "+num_minutes/60  + "h" +nformat3.format(num_minutes%60) +"m ago\n");
				//num_minutes=(int)((t -nowsecs)/(60));
				//outstring.append("Next tide " + h + "m, in " +num_minutes/60  + "h" +nformat3.format(num_minutes%60) +"m\n");
				//outstring.append("---------------\n");
				outstring.append("\n");		//Blank line after current tide section

				//Display Graph
				for (int k = 0; k < num_rows; k++) {
					for (int j = 0; j < num_cols + 1; j++) {
						outstring.append(graph[k][j]);
					}
				}

				//outstring.append("---------------\n");
				outstring.append("\n");		//Blank line after graph


				/* **** Tides table section **** */
				//Display the previous tide
				hightidenext = !hightidenext;
				outstring.append(nformat1.format(hold) + (hightidenext ? " H " : " L ") + dformat.format(new Date(1000 * (long) told)) + '\n');
				//Display the next tide
				hightidenext = !hightidenext;
				outstring.append(nformat1.format(h) + (hightidenext ? " H " : " L ") + dformat.format(new Date(1000 * (long) t)) + '\n');

				for (int k = 0; k < 35 * 4; k++) {
					hightidenext = !hightidenext;
					t = swap(tidedat.readInt());
					h = (float) (tidedat.readByte()) / (float) (10.0);
					outstring.append(nformat1.format(h) + (hightidenext ? " H  " : " L  ") + dformat.format(new Date(1000 * (long) t)) + '\n');
				}
				//Display when the last tide in the file occurs
				outstring.append("The last tide in this datafile occurs at:\n");
				outstring.append(dformat.format(new Date(1000 * (long) lasttide)));
			}

		//Show error message if the tide data file could not be opened for some reason
		}catch (IOException e) {
	        outstring.append("Problem reading tide data\n\n Try selecting the port again, some times the ports available change with and upgrade. If this doesn't work it is either because the tide data is out of date or you've found some bug, try looking for an update.");
			outstring.append("\n  Current date is " + now); 	//added for debugging purposes
			// Printing error
		}	//End of Try Catch
		return outstring.toString();		//save the string to the output array
	}	//end of populating output string





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
        // our menu with their own actions.		   Intent intent = new Intent(null, getIntent().getData());
    	// intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
    	// menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,    new ComponentName(this, NotesList.class), null, intent, 0, null);
        return true;
    }

	/* ** Populate the sub menus ** */
    public boolean  onOptionsItemSelected  (MenuItem  item){
    	  //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	
    	int id = item.getItemId();
	    
	    if(id>=Menu.FIRST+10 && id<Menu.FIRST+10+portlist.length){
	    	currentport = portlist[id-11];			//Extract the currentport from the portlist name pointer
	    	this.onResume();
	    	return true;
	    }
	        
    	switch (id) {
			case MENU_ITEM_ABOUT:				//Display the About string from the strings.xml file
				TextView tv = new TextView(this);
				//tv.setTypeface(Typeface.MONOSPACE);
				tv.setText(R.string.AboutString);	//+now.format2445());
				ScrollView sv = new ScrollView(this);
				sv.addView(tv);
				setContentView(sv);
				//quit();
				return true;
			default:
			return super.onOptionsItemSelected(item);
		}
	}


	/* ** Display outstring on screen after setting the formatting * **/
	@Override
    protected void onResume(){
        String outstring = calc_outstring(currentport);

		//Print the output string to the screen (outstring)
        TextView tv = new TextView(this);
        tv.setTypeface(Typeface.MONOSPACE);         //monospace so as to not break the table layout
		tv.setTextSize(20);                         //Set the font size

		tv.setText(outstring);			//+now.format2445());

        ScrollView sv = new ScrollView(this);
        sv.addView(tv);
        setContentView(sv);   
    	super.onResume();
    }

	
    /* ** Initialise the app ** /
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //restore current port from settings file
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        currentport = settings.getString("CurrentPort","Auckland" );    //Read the CurrentPort field from the prefs file, default to Auckland
        
    //    setContentView(R.layout.main);
    }


	/* ** Save setting on exit ** */
	/* Called when the app is no longer visible to the user (shutdown, minimised, etc) */
    @Override
    protected void onStop(){
       super.onStop();
    
      // Save user preferences. We need an Editor object to
      // make changes. All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);   //Get the shared preferences file and open it to mode 0 = private? Only the app is allowed to read the file
      SharedPreferences.Editor editor = settings.edit();				//Begin the editor
      editor.putString("CurrentPort", currentport);				//set the last used port name

      // Don't forget to commit your edits!!!
      editor.commit();        										//Write changes to the shared preferences file
    }


}




