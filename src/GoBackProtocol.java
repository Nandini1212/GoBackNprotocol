
  
 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.awt.event.ActionEvent; 
import java.awt.*; 
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.applet.Applet; 

public class GoBackProtocol extends Applet implements Runnable, ActionListener { 
	
	
 protected boolean timerOn;
 protected boolean timerPause;   
 protected int startingLoc;
 protected int upcoming;
 protected int moveIncrement;
 protected int currentTarget;
 protected String info; 
 protected ArrayList[] sentPackets;
 protected HashMap<String, Integer> visualParameters = setVisualParams(10, 10, 30, 100, 50, 300, 20, 30);
 protected HashMap<String, Color> colors = setColors(Color.BLUE, Color.GRAY, Color.PINK, Color.BLACK, Color.GREEN, Color.MAGENTA);
 protected Button[] buttons;
 protected Thread[] threads = new Thread[2];
 protected Dimension outsideDim;    
 protected Image outsideImage; 
 protected Graphics outsideG; 
  
 public HashMap<String, Integer> setVisualParams(Integer p1, Integer p2, Integer p3, Integer p4, Integer p5, Integer p6, Integer p7, Integer p8) {
	 HashMap<String, Integer> visualParameters = new HashMap<String, Integer>();
	 visualParameters.put("windowLength", p1);
	 visualParameters.put("packetWidth", p2);
	 visualParameters.put("packetHeight", p3);
	 visualParameters.put("horizontalOffset", p4);
	 visualParameters.put("verticalOffset", p5);
	 visualParameters.put("verticalClearance", p6);
	 visualParameters.put("completePacket", p7);
	 visualParameters.put("timeout", p8);
	 return visualParameters;
	 
 }
 
 public ArrayList createPacket(boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, Integer n) {
		ArrayList newPacket = new ArrayList();
		newPacket.add(b1);
		newPacket.add(b2);
		newPacket.add(b3);
		newPacket.add(b4);
		newPacket.add(b5);
		newPacket.add(n);
		return newPacket;
		
		
	}
 
 
 public void initializeButtons() {
	 buttons = new Button[6];
	 // gen packet button
	 buttons[0] = new Button("Generate Packet"); 
	 buttons[0].setActionCommand("packetGo"); 
	 buttons[0].addActionListener(this); 
	 // pause packets button
	 buttons[1] = new Button("Pause"); 
	 buttons[1].setActionCommand("pause"); 
	 buttons[1].addActionListener(this);
	 // increase packet speed button
	 buttons[2] = new Button("Speed+"); 
	 buttons[2].setActionCommand("fast"); 
	 buttons[2].addActionListener(this);
	 // decrease packet speed button
	 buttons[3] = new Button("Speed-"); 
	 buttons[3].setActionCommand("slow"); 
	 buttons[3].addActionListener(this); 
	 // restart button
	 buttons[4] = new Button("Restart"); 
	 buttons[4].setActionCommand("reset"); 
	 buttons[4].addActionListener(this); 
	 // kill button
	 buttons[5] = new Button("Kill"); 
	 buttons[5].setActionCommand("kill"); 
	 buttons[5].addActionListener(this); 
	 // add buttons to app
	 for (Button b: buttons) {
		 add(b);
	 }
	 
	 
	 
 }
 
 public HashMap<String, Color> setColors(Color c1, Color c2, Color c3, Color c4, Color c5, Color c6) {
	 // initialize colors
	 HashMap<String, Color> colors = new HashMap<String, Color>();
	 colors.put("unacknowledged", c1);
	 colors.put("acknowledged", c2);
	 colors.put("currentTarget", c3);
	 colors.put("roamingPacket", c4);
	 colors.put("roamingAcknowledged", c5);
	 colors.put("destination", c6);
	 return colors; 
 }
  
 public void init() 
  { 
  
  // buttons setup
  initializeButtons();	 
	 
  startingLoc = 0;    
  upcoming = 0;   
  moveIncrement = 5;    
  currentTarget = -1;  
  System.out.println("codeBase=" + this.getCodeBase());


  sentPackets = new ArrayList[visualParameters.get("completePacket")];  
 
  } 
  
  
 public void start() 
  { 
  if (threads[0]==null) threads[0] = new Thread(this); 
  threads[0].start(); 
  } 
  
 public void run() 
  { 
  Thread currenthread = Thread.currentThread(); 
  
  while (currenthread==threads[0])   // started 
   if (onTheWay(sentPackets))    // packets moving? 
    { 
    for (int i=0; i<visualParameters.get("completePacket"); i++) 
     if (sentPackets[i]!= null) 
      if ((boolean) sentPackets[i].get(0))     // packet out and about
       if ((int)sentPackets[i].get(5) < (visualParameters.get("verticalClearance")-visualParameters.get("packetHeight"))) 
        sentPackets[i].set(5, (int)sentPackets[i].get(5) +5);  // increment movement 
       else if ((boolean) sentPackets[i].get(4))  // If it is moving to destination 
        { 
        sentPackets[i].set(2, true); 
        if (check_upto_n(i)) 
         {       // all good, got the packets
         sentPackets[i].set(5, visualParameters.get("packetHeight")+5); 
         sentPackets[i].set(4, false); 
         info = "Packet "+i+" received. Acknowledge sent."; 
         } 
        else 
         { 
         sentPackets[i].set(0, false); 
         info = "Packet "+i+" received. No acknowledge sent."; 
         if (i==currentTarget) 
          { 
          currentTarget = -1; 
        
          } 
         } 
        } 
       else if (!(boolean)sentPackets[i].get(4))   // acknowledged
        { 
        info = "Packet "+ i +" acknowledge received."; 
        sentPackets[i].set(0, false); 
        for (int n=0; n<=i; n++) 
         sentPackets[n].set(3, true); 
        if (i==currentTarget) 
         { 
         currentTarget = -1; 
        
         } 
  
        threads[1] = null;    // timer reset
  
        if (i+visualParameters.get("windowLength")<visualParameters.get("completePacket")) 
         startingLoc = i+1; 
        if (upcoming < startingLoc+visualParameters.get("windowLength")) buttons[0].setEnabled(true); 
  
        if (startingLoc != upcoming) 
         { 
       
         threads[1] = new Thread(this); 
         timerPause = true; 
         threads[1].start(); 
         } 
       
        } 
   
    repaint(); 
  
    try { 
     Thread.sleep(1000/moveIncrement); 
     } catch (Exception e) 
      { 
      System.out.println("There's a problem! The thread wont pause!"); 
      } 
    } 
   else 
    threads[0] = null; 
  
  
  while (currenthread == threads[1]) 
   if (timerPause) 
    { 
    timerPause=false; 
    try { 
     Thread.sleep(visualParameters.get("timeout")*1000); 
     } catch (Exception e) 
       { 
       
       } 
    } 
   else 
    { 
    for (int n=startingLoc; n<startingLoc+visualParameters.get("windowLength"); n++) 
     if (sentPackets[n] != null) 
      if (!(boolean)sentPackets[n].get(3)) 
       { 
       sentPackets[n].set(0, true); 
       sentPackets[n].set(4, true); 
       sentPackets[n].set(5, visualParameters.get("packetHeight")+5); 
       } 
    timerPause = true; 
    if (threads[0] == null) 
     { 
     threads[0] = new Thread (this); 
     threads[0].start(); 
     } 
  
    info = "Packets resent."; 
    } 
  } 
  
  
 public void actionPerformed(ActionEvent e) 
  { 
  String cmd = e.getActionCommand(); 
  
  if (cmd == "packetGo" && upcoming < startingLoc+visualParameters.get("windowLength")) // sent
   { 
   
   sentPackets[upcoming] = createPacket(true, false, false, false, true, visualParameters.get("packetHeight")+5 );
   info = "Packet "+ upcoming +" sent."; 
  
   if (startingLoc == upcoming) 
    {    
   
    if (threads[1] == null) 
     threads[1] = new Thread(this); 
    timerPause = true; 
    threads[1].start(); 
    } 
  
   repaint(); 
   upcoming++; 
   if (upcoming == startingLoc+visualParameters.get("windowLength")) 
    buttons[0].setEnabled(false); 
   start(); 
   } 
  
  else if (cmd == "fast")    // Faster button pressed 
   { 
   moveIncrement+=2; 
   } 
  
  else if (cmd == "slow" && moveIncrement>2) 
   { 
   moveIncrement-=2; 
   
   } 
  
  else if (cmd == "pause") { 
	   threads[0] = null; 
	   if (threads[1] != null) { 
		   timerOn = true; 
		   threads[1] = null;   
	    } 
	   buttons[1].setLabel("Resume"); 
	   buttons[1].setActionCommand("begin"); 
	  
	   
	   buttons[0].setEnabled(false); 
	   buttons[3].setEnabled(false); 
	   buttons[2].setEnabled(false); 
	 
	   repaint(); 
   } 
  
  else if (cmd == "begin") { 
  
	   buttons[1].setLabel("Pause"); 
	   buttons[1].setActionCommand("pause"); 
	   if (timerOn) { 
	    
		    threads[1] = new Thread(this); 
		    timerPause = true; 
		    threads[1].start(); 
	    } 
	  
	 
	   buttons[0].setEnabled(true); 
	   buttons[3].setEnabled(true); 
	   buttons[2].setEnabled(true); 
	  
	   repaint();   
	  
	   start(); 
   } 
  
  else if (cmd == "kill") { 
  
	   sentPackets[currentTarget].set(0, false); 
	   currentTarget = -1;
	   buttons[5].setEnabled(false);
	   repaint(); 
   } 
  
  else if (cmd == "reset") reset_app(); 
  }

  
 public boolean mouseDown(Event e, int x, int y) 
  { 
  int i, xpos, ypos; 
  i = (x-visualParameters.get("horizontalOffset"))/(visualParameters.get("packetWidth")+7); 
  if (sentPackets[i]!= null) 
   { 
   xpos = visualParameters.get("horizontalOffset")+(visualParameters.get("packetWidth")+7)*i; 
   ypos = (int) sentPackets[i].get(5); 
  
   if (x>=xpos && x<= xpos+visualParameters.get("packetWidth") && (boolean)sentPackets[i].get(0)) 
    { 
    if (((boolean)sentPackets[i].get(4) && y>=visualParameters.get("verticalOffset")+ypos && y<=visualParameters.get("verticalOffset")+ypos+visualParameters.get("packetHeight")) || ((!(boolean)sentPackets[i].get(4)) && y>=visualParameters.get("verticalOffset")+visualParameters.get("verticalClearance")-ypos && y<=visualParameters.get("verticalOffset")+visualParameters.get("verticalClearance")-ypos+visualParameters.get("packetHeight"))) 
     { 
     info = "Packet "+ i +" currentTarget."; 
     sentPackets[i].set(1, true); 
     currentTarget = i; 
    
     } 
   
    } 
  
   } 
  return true; 
  } 
 

 public void paint(Graphics g)    // To eliminate flushing, update is overriden 
  { 
  update(g); 
  } 
  
  
 public void update(Graphics g) 
  { 
  Dimension d = new Dimension(500, 500);
  
  
  //Create the offscreen graphics context, if no good one exists. 
        if ((outsideG == null) || (d.width != outsideDim.width) || (d.height != outsideDim.height)) 
   { 
            outsideDim = d; 
            outsideImage = createImage(d.width, d.height); 
            outsideG = outsideImage.getGraphics(); 
   } 
  
  // clear img
        outsideG.setColor(Color.white); 
        outsideG.fillRect(0, 0, d.width, d.height); 
  
  
  for (int i=0; i<visualParameters.get("completePacket"); i++) 
   { 
   // drawing the sending row 
  
   if (sentPackets[i]==null) 
    { 
    outsideG.setColor(Color.black); 
    outsideG.draw3DRect(visualParameters.get("horizontalOffset")+(visualParameters.get("packetWidth")+7)*i, visualParameters.get("verticalOffset"), visualParameters.get("packetWidth"),visualParameters.get("packetHeight"),true); 
    outsideG.draw3DRect(visualParameters.get("horizontalOffset")+(visualParameters.get("packetWidth")+7)*i, visualParameters.get("verticalOffset")+visualParameters.get("verticalClearance"), visualParameters.get("packetWidth"),visualParameters.get("packetHeight"),true); 
    } 
   else 
    { 
    if ((boolean) sentPackets[i].get(3)) 
     outsideG.setColor(colors.get("acknowledged")); 
    else {
     
     outsideG.setColor(colors.get("unacknowledged"));	
    
    }
    outsideG.fill3DRect (visualParameters.get("horizontalOffset")+(visualParameters.get("packetWidth")+7)*i, visualParameters.get("verticalOffset"),visualParameters.get("packetWidth"),visualParameters.get("packetHeight"),true); 
  
  
    // drawing the destination packets 
  
    outsideG.setColor (colors.get("destination")); 
    if ((boolean) sentPackets[i].get(2)) 
     outsideG.fill3DRect (visualParameters.get("horizontalOffset")+(visualParameters.get("packetWidth")+7)*i, visualParameters.get("verticalOffset")+visualParameters.get("verticalClearance"),visualParameters.get("packetWidth"),visualParameters.get("packetHeight"),true); 
    else 
     outsideG.draw3DRect (visualParameters.get("horizontalOffset")+(visualParameters.get("packetWidth")+7)*i, visualParameters.get("verticalOffset")+visualParameters.get("verticalClearance"),visualParameters.get("packetWidth"),visualParameters.get("packetHeight"),true); 
  
    // drawing the moving packets 
  
    if ((boolean) sentPackets[i].get(0)) 
     { 
     if (i==currentTarget) 
      outsideG.setColor (colors.get("currentTarget")); 
     else if ((boolean) sentPackets[i].get(4)) 
      outsideG.setColor (colors.get("roamingPacket")); 
     else 
      outsideG.setColor (colors.get("roamingAcknowledged")); 
  
     if ((boolean) sentPackets[i].get(4)) 
      outsideG.fill3DRect (visualParameters.get("horizontalOffset")+(visualParameters.get("packetWidth")+7)*i, visualParameters.get("verticalOffset")+(int)sentPackets[i].get(5),visualParameters.get("packetWidth"),visualParameters.get("packetHeight"),true); 
     else 
      outsideG.fill3DRect (visualParameters.get("horizontalOffset")+(visualParameters.get("packetWidth")+7)*i, visualParameters.get("verticalOffset")+visualParameters.get("verticalClearance")-(int)sentPackets[i].get(5),visualParameters.get("packetWidth"),visualParameters.get("packetHeight"),true); 
     } 
    } 
   }   // for loop ends 
  
   // drawing message boxes 
  
   outsideG.setColor(Color.BLUE); 
   int vertOffset = visualParameters.get("verticalOffset")+visualParameters.get("verticalClearance")+visualParameters.get("packetHeight"); 
   int horOffset = visualParameters.get("horizontalOffset"); 
  
   outsideG.drawString(info,horOffset,vertOffset+25); 

  
   outsideG.drawString("Packet",horOffset+15,vertOffset+60); 
   outsideG.drawString("Acknowledge",horOffset+85,vertOffset+60); 
   outsideG.drawString("Received Pack",horOffset+185,vertOffset+60); 
   outsideG.drawString("currentTarget",horOffset+295,vertOffset+60); 
  

   outsideG.setColor(Color.YELLOW); 
   outsideG.drawString("sentPackets",visualParameters.get("horizontalOffset")+(visualParameters.get("packetWidth")+7)*visualParameters.get("completePacket")+10,visualParameters.get("verticalOffset")+12); 
   outsideG.drawString("Receiver",visualParameters.get("horizontalOffset")+(visualParameters.get("packetWidth")+7)*visualParameters.get("completePacket")+10,visualParameters.get("verticalOffset")+visualParameters.get("verticalClearance")+12); 
  
   outsideG.setColor(Color.LIGHT_GRAY); 
   outsideG.draw3DRect(horOffset-10,vertOffset+42,360,25,true); 
  
   outsideG.setColor(colors.get("roamingPacket")); 
   outsideG.fill3DRect(horOffset, vertOffset+50,10,10,true); 
  
   outsideG.setColor(colors.get("roamingAcknowledged")); 
   outsideG.fill3DRect(horOffset+70, vertOffset+50,10,10,true); 
  
   outsideG.setColor(colors.get("destination")); 
   outsideG.fill3DRect(horOffset+170, vertOffset+50,10,10,true); 
  
   outsideG.setColor(colors.get("currentTarget")); 
   outsideG.fill3DRect(horOffset+280, vertOffset+50,10,10,true); 
  
  
  g.drawImage(outsideImage, 0, 0, this); 
  }    // method paint ends 
  
  
 // checks out if an array is on the way to source or destination 
 // 0 - on_way; 1 - currentTarget; 2 - reached_dest; 3 - acknowledged; 4 - packet_ack; 5 - packet_pos 
 public boolean onTheWay(ArrayList[] packets)  { 
	  for (int i=0; i<packets.length; i++) 
		  
	    if (packets[i] == null) return false; 
	    else if ((boolean) packets[i].get(0)) return true; 
	  return false; 
  } 
  
 // checkes all the packets before packno. Returns false if any packet has 
 // not reached destination and true if all the packets have reached destination. 
  
 public boolean check_upto_n(int packno) 
  { 
  for (int i=0; i<packno; i++) 
   if (!(boolean)sentPackets[i].get(2)) 
    return false; 
  return true; 
  } 
  
 public void reset_app() 
  { 
  for (int i=0; i<visualParameters.get("completePacket"); i++) 
   if (sentPackets[i] != null) 
    sentPackets[i] = null; 
  startingLoc = 0; 
  upcoming = 0; 
  currentTarget = -1; 
  moveIncrement = 5; 
  timerOn = false; 
  timerPause = false; 
  threads[0] = null; 
  threads[1] = null; 
  if(buttons[1].getActionCommand()=="begin")  // in case of pause mode, enable all buttons 
   { 
   buttons[3].setEnabled(true); 
   buttons[2].setEnabled(true); 
   } 
  
  buttons[0].setEnabled(true); 
 
  
  buttons[1].setLabel("Pause"); 
  buttons[1].setActionCommand("pause"); 
  info = ""; 
  repaint(); 
  } 
 } 





 