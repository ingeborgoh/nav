
import java.io.*;
import java.util.*;
import java.sql.*;

class NavUtils
{
	Com com;
	String debugParam;

	public NavUtils() {}

	public NavUtils(Com com)
	{
		this.com = com;
		debugParam = com.getp("debug");
	}

	// Main metoden
	public static void main(String[] args) throws IOException
	{
		NavUtils nu = new NavUtils();

		if (args.length < 2)
		{
			nu.outl("Arguments: <configFile> <option>\n");
			nu.outl("Where options include:\n");
			//com.outl("   -checkError\t\tSjekk for feil i nettel, swport og subnet tabellene.");
			nu.outl("   -avledTopologi\tAvled topologi med data samlet inn via SNMP.");
			nu.outl("   -avledVlan\tAvled hvilke vlan som kj�rer de de ulike trunkene.");
			return;
		}

		String configFile = args[0];
		ConfigParser cp;
		try {
			cp = new ConfigParser(configFile);
		} catch (IOException e) {
			nu.outl("Error, could not read config file: " + configFile);
			return;
		}
		if (!Database.openConnection(cp.get("SQLServer"), cp.get("SQLDb"), cp.get("SQLUser"), cp.get("SQLPw"))) {
			nu.outl("Error, could not connect to database!");
			return;
		}

		try {
			nu.outl("<html>");
			nu.outl("<head><title>"+args[1].substring(1, args[1].length())+"</title></head>");
			nu.outl("<body>");

			if (args[1].equals("-avledTopologi")) nu.avledTopologi();
			else if (args[1].equals("-avledVlan")) nu.avledVlan();
			else {
				nu.outl("Ikke gyldig argument, start uten agrumenter for hjelp.");
			}

			nu.outl("</body>");
			nu.outl("</html>");

		} catch (SQLException e) {
			nu.outl("SQLException: " + e.getMessage());
		}

	}

	private String mpToIfindex(String modul, String port)
	{
		int m,p;

		try {
			if (modul.startsWith("Fa")) m = Integer.parseInt(modul.substring(2, modul.length()));
			else if (modul.startsWith("Gi")) m = 100+Integer.parseInt(modul.substring(2, modul.length()));
			else m = Integer.parseInt(modul);

			p = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			outl("ERROR, NumberFormatExeption: Modul: " + modul + " Port: " + port);
			return null;
		}
		return String.valueOf(m)+String.valueOf(p);
	}

	public void avledTopologi() throws SQLException
	{
		boolean DEBUG_OUT = false;
		//String debugParam = com.getp("debug");
		if (debugParam != null && debugParam.equals("yes")) DEBUG_OUT = true;
		Boks.DEBUG_OUT = DEBUG_OUT;

		if (DEBUG_OUT) outl("Begin<br>");

		// Vis dato
		{
			java.util.Date currentTime = new GregorianCalendar().getTime();
			outl("Generated on: <b>" + currentTime + "</b><br>");
		}


		//fixPrefiks();
		//if (true) return;

		//String[][] data = db.exece("select nettelid,port,idbak,n1.via3,n1.sysName,n2.sysName from swp_nettel,nettel as n1,nettel as n2 where n1.id=nettelid and n2.id=idbak order by via3,n1.id;");
		//String[][] data = db.exece("select nettelid,port,idbak,n1.via3,n1.sysName,n2.sysName from swp_nettel,nettel as n1,nettel as n2 where n1.id=nettelid and n2.id=idbak and (n1.via3=8 or n1.via3=14 or n1.via3=19) order by via3,n1.id,port;");

		//String[][] data = db.exece("select nettelid,port,idbak,n1.via3,n1.sysName,n2.sysName from swp_nettel,nettel as n1,nettel as n2 where n1.id=nettelid and n2.id=idbak order by via3,n1.id,port;");

		//String[][] data = db.exece("");

		//SELECT nettelid,port,idbak,n1.via3,n1.sysName,n2.sysName from swp_nettel,nettel as n1,nettel as n2 where n1.id=nettelid and n2.id=idbak order by via3,n1.id,port

		//SELECT swp_boks.boksid,modul,port,boksbak,gwport.boksid AS via3,b1.sysName,b2.sysName FROM gwport,swp_boks,boks AS b1,boks AS b2 WHERE b1.boksid=swp_boks.boksid AND b2.boksid=boksbak AND b1.prefiksid=gwport.prefiksid AND gwport.hsrppri='1' ORDER BY b1.prefiksid,b1.boksid,modul,port;
		//String[][] data = db.exece("");

		HashMap boksNavn = new HashMap();
		HashMap boksType = new HashMap();
		HashMap boksKat = new HashMap();
		ResultSet rs = Database.query("SELECT boksid,sysName,typeid,kat FROM boks");
		while (rs.next()) {
			String sysname = rs.getString("sysName"); // M� v�re med da sysname kan v�re null !!
			boksNavn.put(new Integer(rs.getInt("boksid")), (sysname==null?"&lt;null&gt;":sysname) );
			boksType.put(new Integer(rs.getInt("boksid")), rs.getString("typeid"));
			boksKat.put(new Integer(rs.getInt("boksid")), rs.getString("kat"));
		}
		Boks.boksNavn = boksNavn;
		Boks.boksType = boksType;

		//SELECT boksid,sysname,typeid,kat FROM boks WHERE NOT EXISTS (SELECT boksid FROM swp_boks WHERE boksid=boks.boksid) AND (kat='KANT' or kat='SW') ORDER BY boksid

		//SELECT swp_boks.boksid,modul,port,boksbak,gwport.boksid AS gwboksid,b1.sysName,b2.sysName FROM gwport,swp_boks,boks AS b1,boks AS b2 WHERE b1.boksid=swp_boks.boksid AND b2.boksid=boksbak AND b1.prefiksid=gwport.prefiksid AND gwport.hsrppri='1' ORDER BY b1.prefiksid,b1.boksid,modul,port

		//SELECT DISTINCT ON (gwboksid) swp_boks.boksid,modul,port,boksbak,gwport.boksid AS gwboksid FROM (swp_boks JOIN boks USING(boksid)) JOIN gwport USING(prefiksid) WHERE gwport.hsrppri='1' ORDER BY gwboksid,boksid,modul,port
		//SELECT swp_boks.boksid,modul,port,boksbak,gwport.boksid AS gwboksid FROM (swp_boks JOIN boks USING(boksid)) JOIN gwport USING(prefiksid) WHERE gwport.hsrppri='1' ORDER BY boksid,modul,port

		//SELECT swp_boks.boksid,modul,port,boksbak,gwport.boksid AS gwboksid FROM ((swp_boks JOIN boks USING(boksid)) JOIN prefiks USING(prefiksid)) JOIN gwport ON rootgw=gwip ORDER BY boksid,modul,port

		HashSet gwUplink = new HashSet();
		rs = Database.query("SELECT DISTINCT ON (boksbak) boksid,boksbak FROM gwport WHERE boksbak IS NOT NULL");
		while (rs.next()) {
			gwUplink.add(rs.getString("boksbak"));
		}

		rs = Database.query("SELECT swp_boks.boksid,kat,modul,port,swp_boks.boksbak,gwport.boksid AS gwboksid FROM ((swp_boks JOIN boks USING(boksid)) JOIN prefiks USING(prefiksid)) JOIN gwport ON rootgwid=gwportid ORDER BY boksid,modul,port");

		HashMap bokser = new HashMap();
		ArrayList boksList = new ArrayList();
		ArrayList l = null;
		HashSet boksidSet = new HashSet();
		HashSet boksbakidSet = new HashSet();

		//int previd = rs.getInt("boksid");
		int previd = 0;
		while (rs.next()) {
			int boksid = rs.getInt("boksid");
			if (boksid != previd) {
				// Ny boks
				l = new ArrayList();
				boolean isSW = (rs.getString("kat").equals("SW") || rs.getString("kat").equals("GW"));
				Boks b = new Boks(com, boksid, rs.getInt("gwboksid"), l, bokser, isSW, !gwUplink.contains(String.valueOf(boksid)) );
				boksList.add(b);
				previd = boksid;
			}
			String[] s = {
				rs.getString("modul"),
				rs.getString("port"),
				rs.getString("boksbak")
			};
			l.add(s);

			boksidSet.add(new Integer(boksid));
			boksbakidSet.add(new Integer(rs.getInt("boksbak")));
		}

		int maxBehindMp=0;
		for (int i=0; i < boksList.size(); i++) {
			Boks b = (Boks)boksList.get(i);
			bokser.put(b.getBoksidI(), b);
			b.init();
			if (b.maxBehindMp() > maxBehindMp) maxBehindMp = b.maxBehindMp();
		}

		// Legg til alle enheter vi bare har funnet i boksbak
		boksbakidSet.removeAll(boksidSet);
		Iterator iter = boksbakidSet.iterator();
		while (iter.hasNext()) {
			Integer boksbakid = (Integer)iter.next();

			String kat = (String)boksKat.get(boksbakid);
			boolean isSW = (kat.equals("SW") || kat.equals("GW"));

			Boks b = new Boks(com, boksbakid.intValue(), 0, null, bokser, isSW, true);
			bokser.put(b.getBoksidI(), b);
			if (DEBUG_OUT) outl("Adding boksbak("+b.getBoksid()+"): <b>"+b.getName()+"</b><br>");
		}

		if (DEBUG_OUT) outl("Begin processing, maxBehindMp: <b>"+maxBehindMp+"</b><br>");

		for (int level=1; level <= maxBehindMp; level++) {
			boolean done = true;
			for (int i=0; i < boksList.size(); i++) {
				Boks b = (Boks)boksList.get(i);
				if (b.proc_mp(level)) done = false;
			}
			for (int i=0; i < boksList.size(); i++) {
				Boks b = (Boks)boksList.get(i);
				b.removeFromMp();
			}
			if (!done) {
				if (DEBUG_OUT) outl("Level: <b>"+level+"</b>, state changed.<br>");
			}
		}
		// Til slutt sjekker vi uplink-portene, dette vil normalt kun gjelde uplink mot -gw
		for (int i=0; i < boksList.size(); i++) {
			Boks b = (Boks)boksList.get(i);
			b.proc_mp(Boks.PROC_UPLINK_LEVEL);
		}

		if (DEBUG_OUT) outl("<b>BEGIN REPORT</b><br>");
		for (int i=0; i < boksList.size(); i++) {
			Boks b = (Boks)boksList.get(i);
			if (DEBUG_OUT) b.report();
			b.guess();
		}
		HashMap boksMp = new HashMap();
		for (int i=0; i < boksList.size(); i++) {
			Boks b = (Boks)boksList.get(i);
			b.addToMp(boksMp);
		}
		if (DEBUG_OUT) outl("Report done.<br>");

		// Vi m� vite hvilke bokser som har trunker ut fra seg, dvs. det kj�rer flere vlan
		HashSet boksWithTrunk = new HashSet();
		rs = Database.query("SELECT DISTINCT boksid FROM swport WHERE trunk='t'");
		while (rs.next()) boksWithTrunk.add(rs.getString("boksid"));

		// Vi trenger en oversikt over hvilket vlan de forskjellige boksene er p�
		HashMap boksVlan = new HashMap();
		rs = Database.query("SELECT boksid,vlan FROM boks JOIN prefiks USING (prefiksid) WHERE vlan IS NOT NULL");
		while (rs.next()) {
			boksVlan.put(rs.getString("boksid"), rs.getString("vlan"));
		}

		// N� g�r vi gjennom alle portene vi har funnet boksbak for, og oppdaterer tabellen med dette
		int newcnt=0,updcnt=0,remcnt=0;
		ArrayList swport = new ArrayList();
		HashMap swrecMap = new HashMap();
		//rs = Database.query("SELECT swportid,boksid,status,speed,duplex,modul,port,portnavn,boksbak,static,trunk,hexstring FROM swport NATURAL LEFT JOIN swportallowedvlan WHERE status='up' ORDER BY boksid,modul,port");
		rs = Database.query("SELECT swportid,boksid,status,speed,duplex,modul,port,portnavn,boksbak,static,trunk,hexstring FROM swport NATURAL LEFT JOIN swportallowedvlan ORDER BY boksid,modul,port");
		ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next()) {
			HashMap hm = getHashFromResultSet(rs, rsmd);
			if (!rs.getString("status").toLowerCase().equals("down")) swport.add(hm);
			String key = rs.getString("boksid")+":"+rs.getString("modul")+":"+rs.getString("port");
			swrecMap.put(key, hm);
		}

		if (DEBUG_OUT) outl("boksMp listing....<br>");
		iter = boksMp.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry me = (Map.Entry)iter.next();
			String key = (String)me.getKey();
			Integer boksbak = (Integer)me.getValue();

			StringTokenizer st = new StringTokenizer(key, ":");
			String boksid = st.nextToken();
			String modul = st.nextToken();
			String port = st.nextToken();

			//outl(boksNavn.get(new Integer(boksid)) + " Modul: " + modul + " Port: " + port + " Link: " + boksNavn.get(boksbak) + "<br>");

			if (swrecMap.containsKey(key)) {
				// Record eksisterer fra f�r, sjekk om oppdatering er n�dvendig
				HashMap swrec = (HashMap)swrecMap.get(key);
				//swrecMap.remove(key);
				swrec.put("deleted", null);

				String status = (String)swrec.get("status");
				if (status.toLowerCase().equals("down")) continue;

				String idbak = (String)swrec.get("boksbak");
				if (idbak == null || idbak != null && Integer.parseInt(idbak) != boksbak.intValue()) {
					// Oppdatering n�dvendig
					updcnt++;
					// swport
					{
						String[] updateFields = {
							"boksbak", boksbak.toString()
						};
						String[] condFields = {
							"swportid", (String)swrec.get("swportid")
						};
						Database.update("swport", updateFields, condFields);
					}

					String vlan = "non-s";
					if (swrec.get("static").equals("t")) {
						if (swrec.get("trunk").equals("t")) {
							// Vi har en static trunk, pr�v � finn record for andre veien
							Boks b = (Boks)bokser.get(boksbak);
							Mp uplinkMp = b.getMpTo(new Integer(boksid));
							if (uplinkMp != null) {
								// Port funnet, men eksisterer denne porten i tabellen fra f�r?
								String keyBak = boksbak+":"+uplinkMp;
								if (swrecMap.containsKey(keyBak)) {
									// Eksisterer fra f�r, sjekk om det er en trunk
									HashMap swrecBak = (HashMap)swrecMap.get(keyBak);
									if ("t".equals(swrecBak.get("trunk"))) {
										// Trunk, sjekk om vi m� oppdatere swportallowedvlan
										String allowedVlan = (String)swrec.get("hexstring");
										String allowedVlanBak = (String)swrecBak.get("hexstring");
										if (!allowedVlan.equals(allowedVlanBak)) {
											// Oppdatering n�dvendig
											String[] updateFields = {
												"hexstring", allowedVlan
											};
											String[] condFields = {
												"swportid", (String)swrec.get("swportid")
											};
											Database.update("swportallowedvlan", updateFields, condFields);
											if (DEBUG_OUT) outl("Updated swportallowedvlan, swportid: " + condFields[0] + " hexstring: " + allowedVlan + "<br>");
										}
									}
								}
							}
							vlan = "trunk";

						} else {
							// swportvlan
							if (boksWithTrunk.contains(boksbak.toString())) {
								// Boksen i andre enden har trunk, da m� vi bruke v�rt eget vlan
								vlan = (String)boksVlan.get(boksid);
							} else {
								vlan = (String)boksVlan.get(boksbak.toString());
							}
							if (vlan != null) {
								String[] updateFields = {
									"vlan", vlan,
									"retning", "s"
								};
								String[] condFields = {
									"swportid", (String)swrec.get("swportid")
								};
								Database.update("swportvlan", updateFields, condFields);
							}
						}
					}

					swrec.put("boksbak", boksbak.toString());
					swrec.put("change", "Updated ("+vlan+")");
				}
			} else {
				// Record eksister ikke, og m� derfor settes inn

				// F�rst m� vi sjekke om andre siden er en trunk
				String vlan;
				String trunk = "f";
				String allowedVlan = null;
				{
					Boks b = (Boks)bokser.get(boksbak);
					Mp uplinkMp = b.getMpTo(new Integer(boksid));
					if (uplinkMp != null) {
						// Port funnet, men eksisterer denne porten i tabellen fra f�r?
						String keyBak = boksbak+":"+uplinkMp;
						if (swrecMap.containsKey(keyBak)) {
							// Eksisterer fra f�r, sjekk om det er en trunk
							HashMap swrecBak = (HashMap)swrecMap.get(keyBak);
							if ("t".equals(swrecBak.get("trunk"))) {
								// Trunk, da m� vi ogs� sette inn i swportallowedvlan
								trunk = "t";
								allowedVlan = (String)swrecBak.get("hexstring");
							}
						}
					}

					if (trunk.equals("t")) {
						vlan = "t";
					} else if (boksWithTrunk.contains(boksbak.toString())) {
						// Boksen i andre enden har trunk, da m� vi bruke v�rt eget vlan
						vlan = (String)boksVlan.get(boksid);
					} else {
						vlan = (String)boksVlan.get(boksbak.toString());
					}

					if (vlan != null) {
						// Vi setter kun inn i swport hvis vi vet vlan eller det er en trunk det er snakk om
						// swport
						String ifind = mpToIfindex(modul, port);
						if (ifind != null) {
							String[] insertFields = {
								"boksid", boksid,
								"ifindex", ifind,
								"status", "up",
								"trunk", trunk,
								"static", "t",
								"modul", modul,
								"port", port,
								"boksbak", boksbak.toString()
							};
							if (!Database.insert("swport", insertFields)) {
								outl("<font color=\"red\">Error with insert, boksid=" + boksid + " trunk="+trunk+" ifindex=" + insertFields[1] + " modul="+modul+" port="+port+" boksbak="+boksbak+"</font><br>");
							} else {
								if (DEBUG_OUT) outl("Inserted row, boksid=" + boksid + " trunk="+trunk+" ifindex="+insertFields[1]+" modul="+modul+" port="+port+" boksbak="+boksbak+"<br>");
								//Database.commit();
								newcnt++;
							}
						}

					}


				}

				// Hvis trunk setter vi inn i swportallowedvlan, ellers rett inn i swportvlan
				if (trunk.equals("t")) {
					// swportallowedvlan
					String sql = "INSERT INTO swportallowedvlan (swportid,hexstring) VALUES ("+
								 "(SELECT swportid FROM swport WHERE boksid='"+boksid+"' AND modul='"+modul+"' AND port='"+port+"' AND boksbak='"+boksbak+"'),"+
								 "'"+allowedVlan+"')";
					Database.update(sql);
					if (DEBUG_OUT) outl("swportallowedvlan: "+sql+"<br>");

				} else
				if (vlan != null) {
				// swportvlan
				// Hvilket vlan g�r over linken? Vi henter vlanet boksbak er p�

					// Siden vi ikke vet fremmedn�kkelen m� vi bruke sub-select her
					String sql = "INSERT INTO swportvlan (swportid,vlan,retning) VALUES ("+
								 "(SELECT swportid FROM swport WHERE boksid='"+boksid+"' AND modul='"+modul+"' AND port='"+port+"' AND boksbak='"+boksbak+"'),"+
								 "'"+vlan+"',"+
								 "'s')";
					Database.update(sql);
					if (DEBUG_OUT) outl("swportvlan: "+sql+"<br>");
				}


				// Lag swrec
				HashMap swrec = new HashMap();
				swrec.put("swportid", "N/A");
				swrec.put("boksid", boksid);
				swrec.put("status", "up");
				swrec.put("speed", null);
				swrec.put("duplex", null);
				swrec.put("modul", modul);
				swrec.put("port", port);
				swrec.put("portnavn", null);
				swrec.put("boksbak", boksbak.toString());
				swrec.put("static", "t");
				if (vlan != null) {
					swrec.put("change", "Inserted ("+vlan+")");
				} else {
					swrec.put("change", "Error, vlan is null");
				}

				swport.add(swrec);
				//swrecMap.put(key, swrec);
			}

		}
		if (DEBUG_OUT) outl("boksMp listing done.<br>");

		iter = swrecMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry me = (Map.Entry)iter.next();
			String key = (String)me.getKey();
			HashMap swrec = (HashMap)me.getValue();

			if (!swrec.get("static").equals("t")) continue;
			if (swrec.containsKey("deleted")) continue;

			remcnt++;

			StringTokenizer st = new StringTokenizer(key, ":");
			String boksid = st.nextToken();
			String modul = st.nextToken();
			String port = st.nextToken();

			String swportid = (String)swrec.get("swportid");

			// boksbak_s kan egentlig ikke v�re null, men for sikkerhets skyld
			String boksbak_s = (String)swrec.get("boksbak");
			Integer boksbak = (boksbak_s == null) ? new Integer(0-1) : new Integer((String)swrec.get("boksbak"));

			Database.update("DELETE FROM swport WHERE swportid='"+swportid+"'");
			if (DEBUG_OUT) outl("[DELETED] swportid: <b>"+swportid+"</b> sysName: <b>" + boksNavn.get(new Integer(boksid)) + "</b> Modul: <b>" + modul + "</b> Port: <b>" + port + "</b> Link: <b>" + boksNavn.get(boksbak) + "</b> Static: <b>" + swrec.get("static") + "</b><br>");
		}

		outl("<table>");
		outl("  <tr>");
		outl("    <td><b>swpid</b></td>");
		outl("    <td><b>boksid</b></td>");
		outl("    <td><b>sysName</b></td>");
		outl("    <td><b>typeId</b></td>");
		outl("    <td><b>Speed</b></td>");
		outl("    <td><b>Duplex</b></td>");
		outl("    <td><b>Modul</b></td>");
		outl("    <td><b>Port</b></td>");
		outl("    <td><b>Portnavn</b></td>");
		outl("    <td><b>Boksbak</b></td>");
		outl("    <td><b>Change (vlan)</b></td>");
		outl("  </tr>");

		int attCnt=0;
		for (int i=0; i < swport.size(); i++) {
			HashMap swrec = (HashMap)swport.get(i);
			String boksid = (String)swrec.get("boksid");
			String modul = (String)swrec.get("modul");
			String port = (String)swrec.get("port");
			String portnavn = (String)swrec.get("portnavn");
			boolean isStatic = swrec.get("static").equals("t");
			String change = (String)swrec.get("change");

			String boksbak = "";
			Integer idbak = (Integer)boksMp.get(boksid+":"+modul+":"+port);
			if (idbak != null) boksbak = (String)boksNavn.get(idbak);

			if (portnavn == null) portnavn = "";

			if (boksbak == null) {
				outl("ERROR! boksbak is null for idbak: " + idbak + "<br>");
				continue;
			}

			String color = "gray";

			if (change != null && change.startsWith("Error")) {
				color = "red";
			} else
			if (portnavn.length() == 0 && boksbak.length()>0) {
				color = "blue";
			} else
			if (portnavn.length() > 0 && boksbak.length()==0) {
				if (portnavn.indexOf("-h") != -1 || portnavn.indexOf("-sw") != -1 || portnavn.indexOf("-gw") != -1) {
					color = "purple";
				}
			} else
			if (portnavn.length() > 0 && boksbak.length()>0 && portnavn.endsWith(boksbak) ) {
				color = "green";
			} else
			if (portnavn.length() > 0 && boksbak.length()>0 && !portnavn.endsWith(boksbak) ) {
				color = "red";
			}

			if (!color.equals("purple") && !color.equals("red")) continue;
			if (portnavn.length() > 2 && portnavn.charAt(0) == 'n' && portnavn.charAt(2) == ':') continue;

			attCnt++;
			String color1 = "<font color="+color+">";
			String color2 = "</font>";

			outl("<tr>");
			//outl("<td align=right>"+color1+ swrec.get("swportid") + color2+"</td>");
			outl("<td align=right><a href=\"#" + swrec.get("swportid") + "\">" + swrec.get("swportid") + "</a></td>");
			outl("<td align=right>"+color1+ swrec.get("boksid") + color2+"</td>");
			outl("<td>"+color1+ boksNavn.get(new Integer((String)swrec.get("boksid"))) + color2+"</td>");
			outl("<td>"+color1+ boksType.get(new Integer((String)swrec.get("boksid"))) + color2+"</td>");
			outl("<td align=right>"+color1+ swrec.get("speed") + color2+"</td>");
			outl("<td align=right>"+color1+ swrec.get("duplex") + color2+"</td>");
			outl("<td align=right>"+color1+ swrec.get("modul") + color2+"</td>");
			outl("<td align=right>"+color1+ swrec.get("port") + color2+"</td>");
			outl("<td>"+color1+ portnavn + color2+"</td>");
			outl("<td>"+color1+ boksbak + color2+"</td>");

			if (change != null) outl("<td><b>"+change+"</b></td>");

			outl("</tr>");
		}
		outl("</table>");
		outl("Found <b>" + attCnt + "</b> rows in need of attention.<br>");

		outl("<h2>swport:</h2>");
		outl("<table>");
		outl("  <tr>");
		outl("    <td><b>swpid</b></td>");
		outl("    <td><b>boksid</b></td>");
		outl("    <td><b>sysName</b></td>");
		outl("    <td><b>Speed</b></td>");
		outl("    <td><b>Duplex</b></td>");
		outl("    <td><b>Modul</b></td>");
		outl("    <td><b>Port</b></td>");
		outl("    <td><b>Portnavn</b></td>");
		outl("    <td><b>Boksbak</b></td>");
		outl("    <td><b>Change (vlan)</b></td>");
		outl("  </tr>");

		for (int i=0; i < swport.size(); i++) {
			HashMap swrec = (HashMap)swport.get(i);
			String boksid = (String)swrec.get("boksid");
			String modul = (String)swrec.get("modul");
			String port = (String)swrec.get("port");
			String portnavn = (String)swrec.get("portnavn");
			boolean isStatic = swrec.get("static").equals("t");
			String change = (String)swrec.get("change");

			String boksbak = "";
			Integer idbak = (Integer)boksMp.get(boksid+":"+modul+":"+port);
			if (idbak != null) boksbak = (String)boksNavn.get(idbak);

			if (portnavn == null) portnavn = "";

			if (boksbak == null) {
				outl("ERROR! boksbak is null for idbak: " + idbak + "<br>");
				continue;
			}

			String color = "gray";

			if (change != null && change.startsWith("Error")) {
				color = "red";
			} else
			if (portnavn.length() == 0 && boksbak.length()>0) {
				color = "blue";
			} else
			if (portnavn.length() > 0 && boksbak.length()==0) {
				if (portnavn.indexOf("-h") != -1 || portnavn.indexOf("-sw") != -1 || portnavn.indexOf("-gw") != -1) {
					color = "purple";
				}
			} else
			if (portnavn.length() > 0 && boksbak.length()>0 && portnavn.endsWith(boksbak) ) {
				color = "green";
			} else
			if (portnavn.length() > 0 && boksbak.length()>0 && !portnavn.endsWith(boksbak) ) {
				color = "red";
			}

			String color1 = "<font color="+color+">";
			String color2 = "</font>";

			outl("<tr><a name=\"" + swrec.get("swportid") + "\">");
			outl("<td align=right>"+color1+ swrec.get("swportid") + color2+"</td>");
			outl("<td align=right>"+color1+ swrec.get("boksid") + color2+"</td>");
			outl("<td>"+color1+ boksNavn.get(new Integer((String)swrec.get("boksid"))) + color2+"</td>");
			outl("<td align=right>"+color1+ swrec.get("speed") + color2+"</td>");
			outl("<td align=right>"+color1+ swrec.get("duplex") + color2+"</td>");
			outl("<td align=right>"+color1+ swrec.get("modul") + color2+"</td>");
			outl("<td align=right>"+color1+ swrec.get("port") + color2+"</td>");
			outl("<td>"+color1+ portnavn + color2+"</td>");
			outl("<td>"+color1+ boksbak + color2+"</td>");

			if (change != null) outl("<td><b>"+change+"</b></td>");

			outl("</tr>");
		}
		outl("</table>");

		outl("New rows: <b>" + newcnt + "</b> Updated rows: <b>" + updcnt + "</b> Removed rows: <b>"+remcnt+"</b><br>");
		if (newcnt > 0 || updcnt > 0 || remcnt > 0) {
			if (DEBUG_OUT) outl("** COMMIT ON DATABASE **<br>");
			Database.commit();
		}
		//Database.rollback();


		outl("All done.<br>");

	}

	private HashMap getHashFromResultSet(ResultSet rs, ResultSetMetaData md) throws SQLException
	{
		HashMap hm = new HashMap();
		for (int i=md.getColumnCount(); i > 0; i--) {
			hm.put(md.getColumnName(i), rs.getString(i));
		}
		return hm;
	}

	/* [/ni.avledVlan]
	 *
	 */
	public void avledVlan() throws SQLException
	{
		boolean DB_UPDATE = true;
		boolean DB_COMMIT = true;
		boolean DEBUG_OUT = false;

		//String debugParam = com.getp("debug");
		if (debugParam != null && debugParam.equals("yes")) DEBUG_OUT = true;
		if (DEBUG_OUT) outl("Begin<br>");

		// Vis dato
		{
			java.util.Date currentTime = new GregorianCalendar().getTime();
			outl("Generated on: <b>" + currentTime + "</b><br>");
		}

		// Vi starter med � sette boksbak til null alle steder hvor status='down', slik at vi unng�r l�kker
		{
			Database.update("UPDATE swport SET boksbak = NULL WHERE status='down' AND boksbak IS NOT NULL");
			Database.commit();
		}

		// Denne er egentlig bare n�dvendig for debugging
		HashMap boksName = new HashMap();
		ResultSet rs = Database.query("SELECT boksid,sysname FROM boks");
		while (rs.next()) boksName.put(rs.getString("boksid"), rs.getString("sysname"));

		// Trenger � vite hva som er GW, alle linker til slike er nemlig 'o' og de skal ikke traverseres
		HashSet boksGwSet = new HashSet();
		rs = Database.query("SELECT boksid FROM boks WHERE kat='GW'");
		while (rs.next()) boksGwSet.add(rs.getString("boksid"));

		// Oversikt over hvilke vlan som kj�rer p� en swport mot gw
		HashSet swportGwVlanSet = new HashSet();
		rs = Database.query("SELECT DISTINCT swportbak,vlan FROM gwport JOIN prefiks USING(prefiksid) WHERE swportbak IS NOT NULL AND vlan IS NOT NULL");
		while (rs.next()) swportGwVlanSet.add(rs.getString("swportbak")+":"+rs.getString("vlan"));

		// Oversikt over hvilke linker:vlan som er blokkert av spanning tree
		HashSet spanTreeBlocked = new HashSet();
		rs = Database.query("SELECT swportid,vlan FROM swportblocked");
		while (rs.next()) spanTreeBlocked.add(rs.getString("swportid")+":"+rs.getString("vlan"));

		// Oversikt over ikke-trunker ut fra hver boks per vlan
		HashMap nontrunkVlan = new HashMap();
		rs = Database.query("SELECT swportid,boksid,boksbak,vlan FROM swport NATURAL JOIN swportvlan WHERE trunk='f' AND boksbak IS NOT NULL");
		while (rs.next()) {
			HashMap nontrunkMap;
			String key = rs.getString("boksid")+":"+rs.getString("vlan");
			if ( (nontrunkMap = (HashMap)nontrunkVlan.get(key)) == null) {
				nontrunkMap = new HashMap();
				nontrunkVlan.put(key, nontrunkMap);
			}
			HashMap hm = new HashMap();
			hm.put("swportid", rs.getString("swportid"));
			hm.put("boksid", rs.getString("boksid"));
			//hm.put("modul", rs.getString("modul"));
			//hm.put("port", rs.getString("port"));
			hm.put("boksbak", rs.getString("boksbak"));
			nontrunkMap.put(rs.getString("boksbak"), hm);
		}

		// F�rst m� vi hente oversikten over hvilke vlan som kan kj�re p� de forskjellige portene
		HashMap allowedVlan = new HashMap();
		//ResultSet rs = Database.query("SELECT boksid,modul,port,portnavn,boksbak,substring(hexstring from 250) FROM swport NATURAL JOIN swportallowedvlan WHERE boksbak IS NOT null");
		rs = Database.query("SELECT boksid,swportid,modul,port,boksbak,hexstring FROM swport NATURAL JOIN swportallowedvlan WHERE boksbak IS NOT null");

		while (rs.next()) {
			HashMap boksAllowedMap;
			String boksid = rs.getString("boksid");
			if ( (boksAllowedMap = (HashMap)allowedVlan.get(boksid)) == null) {
				boksAllowedMap = new HashMap();
				allowedVlan.put(boksid, boksAllowedMap);
			}
			HashMap hm = new HashMap();
			hm.put("swportid", rs.getString("swportid"));
			hm.put("boksid", rs.getString("boksid"));
			hm.put("modul", rs.getString("modul"));
			hm.put("port", rs.getString("port"));
			hm.put("boksbak", rs.getString("boksbak"));
			hm.put("hexstring", rs.getString("hexstring"));
			boksAllowedMap.put(rs.getString("boksbak"), hm);
		}

		// Vi trenger � vite hvilke vlan som g�r ut p� ikke-trunk fra en gitt boks
		// Bruker da en HashMap av HashSets
		HashMap activeVlan = new HashMap();
		rs = Database.query("SELECT DISTINCT boksid,vlan FROM swport JOIN swportvlan USING (swportid) WHERE trunk='f' AND status='up'");
		while (rs.next()) {
			HashSet hs;
			String boksid = rs.getString("boksid");
			if ( (hs = (HashSet)activeVlan.get(boksid)) == null) {
				hs = new HashSet();
				activeVlan.put(boksid, hs);
			}
			hs.add(new Integer(rs.getInt("vlan")));
		}

		// S� henter vi ut alle vlan og hvilken switch vlanet "starter p�"
		outl("<pre>");
		//rs = Database.query("SELECT DISTINCT ON (vlan) vlan,boks.sysname,swport.boksid,portnavn FROM ((prefiks JOIN gwport ON (rootgwid=gwportid)) JOIN boks USING (boksid)) JOIN swport ON (SUBSTRING(portnavn FROM 3)=sysname) WHERE portnavn LIKE 'o:%-gw%' AND vlan IS NOT null ORDER BY vlan");
		//rs = Database.query("SELECT DISTINCT ON (vlan) vlan,sysname,boksbak FROM (prefiks JOIN gwport USING (prefiksid)) JOIN boks USING (boksid) WHERE boksbak IS NOT NULL AND vlan IS NOT NULL ORDER BY vlan");
		//rs = Database.query("SELECT DISTINCT ON (vlan) gwport.boksid,vlan,sysname,gwport.boksbak,swportid,hexstring FROM (prefiks JOIN gwport ON (rootgwid=gwportid)) JOIN boks USING (boksid) LEFT JOIN swport ON (gwport.boksbak=swport.boksid AND swport.boksbak=gwport.boksid AND trunk='t') LEFT JOIN swportallowedvlan USING (swportid) WHERE gwport.boksbak IS NOT NULL AND vlan IS NOT NULL ORDER BY vlan");
		rs = Database.query("SELECT DISTINCT ON (vlan) gwport.boksid,vlan,sysname,gwport.boksbak,swportbak,trunk,hexstring FROM (prefiks JOIN gwport ON (rootgwid=gwportid)) JOIN boks USING (boksid) JOIN swport ON (swportbak=swportid) LEFT JOIN swportallowedvlan USING (swportid) WHERE gwport.boksbak IS NOT NULL AND vlan IS NOT NULL ORDER BY vlan");

		// Newer
		// SELECT DISTINCT ON (vlan) gwport.boksid,vlan,sysname,gwport.boksbak,swportbak,trunk,hexstring FROM (prefiks JOIN gwport ON (rootgwid=gwportid)) JOIN boks USING (boksid) JOIN swport ON (swportbak=swportid) LEFT JOIN swportallowedvlan USING (swportid) WHERE gwport.boksbak IS NOT NULL AND vlan IS NOT NULL ORDER BY vlan

		// New
		// SELECT DISTINCT ON (vlan) gwport.boksid,vlan,sysname,gwport.boksbak,swportid,hexstring FROM (prefiks JOIN gwport ON (rootgwid=gwportid)) JOIN boks USING (boksid) LEFT JOIN swport ON (gwport.boksbak=swport.boksid AND swport.boksbak=gwport.boksid AND trunk='t') LEFT JOIN swportallowedvlan USING (swportid) WHERE gwport.boksbak IS NOT NULL AND vlan IS NOT NULL ORDER BY vlan
		// Old
		// SELECT DISTINCT ON (vlan) gwport.boksid,vlan,sysname,gwport.boksbak,swportid,hexstring FROM (prefiks JOIN gwport USING (prefiksid)) JOIN boks USING (boksid) LEFT JOIN swport ON (gwport.boksbak=swport.boksid AND swport.boksbak=gwport.boksid AND trunk='t') LEFT JOIN swportallowedvlan USING (swportid) WHERE gwport.boksbak IS NOT NULL AND vlan IS NOT NULL ORDER BY vlan

		ArrayList trunkVlan = new ArrayList();
		// ***** BEGIN DEPTH FIRST SEARCH ***** //
		while (rs.next()) {
			int vlan = rs.getInt("vlan");
			String boksid = rs.getString("boksid");
			String boksbak = rs.getString("boksbak");
			String swportbak = rs.getString("swportbak");
			boolean cameFromTrunk = rs.getBoolean("trunk");
			String hexstring = rs.getString("hexstring");
			if (DEBUG_OUT) outl("\n<b>NEW VLAN: " + vlan + "</b><br>");

			// Sjekk om det er en trunk eller ikke-trunk ned til gw'en
			if (cameFromTrunk) {
				// N� forventer vi at hexstring er p� plass
				if (hexstring == null) {
					if (DEBUG_OUT) outl("\n<b>AllowedVlan hexstring for trunk down to switch is missing, skipping...</b><br>");
					continue;
				}
				// Sjekk vi om vi faktisk har lov til � kj�re p� trunken
				if (!isAllowedVlan(hexstring, vlan)) {
					if (DEBUG_OUT) outl("\n<b>Vlan is not allowed on trunk down to switch, and there is no non-trunk, skipping...</b><br>");
					continue;
				}
			}

			/*
			// Vi m� n� sjekke om det er en ikke-trunk opp fra switchen til denne gw'en p� dette vlan'et
			String key = boksbak+":"+vlan;
			HashMap nontrunkMap = (HashMap)nontrunkVlan.get(key);
			if (nontrunkMap != null) {
				// Det er porter p� vlanet ihvertfall, men er det noen til gw'en?
				HashMap swrec = (HashMap)nontrunkMap.get(boksid);
				if (swrec != null) {
					// Jo, ok, da lagrer vi den virkelige swportid'en
					 swportid = (String)swrec.get("swportid");
					 cameFromTrunk = false;
				 }
			 }
			 if (cameFromTrunk) {
				 // Det er ikke en ikke-trunk mellom gw og sw, alts� m� det v�re en trunk
				 // Da m� vi f�rst sjekke at vlanet har lov til � kj�re
				if (rs.getString("hexstring") == null) {
					if (DEBUG_OUT) outl("\n<b>AllowedVlan hexstring for trunk down to switch is missing, and there is no non-trunk, skipping...</b><br>");
					continue;
				} else if (!isAllowedVlan(rs.getString("hexstring"), vlan)) {
					if (DEBUG_OUT) outl("\n<b>Vlan is not allowed on trunk down to switch, and there is no non-trunk, skipping...</b><br>");
					continue;
				}
				// OK, vi har lov til � kj�re p� trunken, lagre swportid for denne
				swportid = rs.getString("swportid");
			}
			*/

			// S� traverserer vi linken ned til sw'en

			//  vlanTraverseLink(int vlan, String fromid, String boksid, boolean cameFromTrunk, boolean setDirection, HashMap nontrunkVlan, HashMap allowedVlan, HashMap activeVlan, HashSet spanTreeBlocked, ArrayList trunkVlan, HashSet visitNode, int level, Com com, boolean DEBUG_OUT, HashMap boksName)
			if (vlanTraverseLink(vlan, boksid, boksbak, cameFromTrunk, true, nontrunkVlan, allowedVlan, activeVlan, spanTreeBlocked, trunkVlan, new HashSet(), 0, com, DEBUG_OUT, boksGwSet, swportGwVlanSet, boksName)) {

				// Vlanet er aktivt p� enheten, s� da legger vi det til
				String[] tvlan = {
					swportbak,
					String.valueOf(vlan),
					"o"
				};
				trunkVlan.add(tvlan);
			}
		}



		// Alle vlan som vi ikke finner startpunkt p�, hver m� vi rett og slett starte alle andre steder for � v�re sikker p� � f� med alt
		// SELECT DISTINCT ON (vlan,boksid) boksid,modul,port,boksbak,vlan,trunk FROM swport NATURAL JOIN swportvlan WHERE vlan NOT IN (SELECT DISTINCT vlan FROM (prefiks JOIN gwport USING (prefiksid)) JOIN boks USING (boksid) WHERE boksbak IS NOT NULL AND vlan IS NOT NULL) AND boksbak IS NOT NULL ORDER BY vlan,boksid
		if (DEBUG_OUT) outl("\n<b>VLANs with no router to start from:</b><br>");
		rs = Database.query("SELECT DISTINCT ON (vlan,boksid) vlan,sysname,boksbak FROM swport NATURAL JOIN swportvlan JOIN boks USING (boksid) WHERE vlan NOT IN (SELECT DISTINCT vlan FROM (prefiks JOIN gwport USING (prefiksid)) JOIN boks USING (boksid) WHERE boksbak IS NOT NULL AND vlan IS NOT NULL) AND boksbak IS NOT NULL ORDER BY vlan");
		while (rs.next()) {
			int vlan = rs.getInt("vlan");
			if (DEBUG_OUT) outl("\n<b>NEW VLAN: " + vlan + "</b><br>");
			vlanTraverseLink(vlan, null, rs.getString("boksbak"), true, false, nontrunkVlan, allowedVlan, activeVlan, spanTreeBlocked, trunkVlan, new HashSet(), 0, com, DEBUG_OUT, boksGwSet, swportGwVlanSet, boksName);
		}

		outl("</pre>");

		HashMap swportvlan = new HashMap();
		HashMap swportvlanDupe = new HashMap();
		//rs = Database.query("SELECT swportvlanid,swportid,vlan,retning FROM swportvlan JOIN swport USING (swportid) WHERE swport.trunk='t'");
		rs = Database.query("SELECT swportvlanid,swportid,vlan,retning,trunk FROM swportvlan JOIN swport USING (swportid)");
		while (rs.next()) {
			String key = rs.getString("swportid")+":"+rs.getString("vlan");
			swportvlanDupe.put(key, rs.getString("retning") );

			// Kun vlan som g�r over trunker skal evt. slettes
			if (rs.getBoolean("trunk")) swportvlan.put(key, rs.getString("swportvlanid") );

		}

		outl("<br><b>Report:</b> (found "+trunkVlan.size()+" records)<br>");

		HashMap activeOnTrunk = new HashMap(); // Denne brukes for � sjekke swportallowedvlan mot det som faktisk kj�rer

		int newcnt=0,updcnt=0,dupcnt=0,remcnt=0;
		for (int i=0; i < trunkVlan.size(); i++) {
			String[] s = (String[])trunkVlan.get(i);
			String swportid = s[0];
			String vlan = s[1];
			String retning = s[2];
			String key = swportid+":"+vlan;

			if (swportvlanDupe.containsKey(key)) {
				// Elementet eksisterer i databasen fra f�r, s� vi skal ikke sette det inn
				// Sjekk om vi skal oppdatere
				String dbRetning = (String)swportvlanDupe.get(key);
				if (!dbRetning.equals(retning)) {
					// Oppdatering n�dvendig
					String[] updateFields = {
						"retning", retning
					};
					String[] condFields = {
						"swportid", swportid,
						"vlan", vlan
					};
					Database.update("swportvlan", updateFields, condFields);
					outl("[UPD] swportid: " + swportid + " vlan: <b>"+ vlan +"</b> Retning: <b>" + retning + "</b> (old: "+dbRetning+")<br>");
					updcnt++;
				} else {
					dupcnt++;
				}
				// Vi skal ikke slette denne recorden n�
				swportvlan.remove(key);


			} else {
				// swportvlan inneholder ikke dette innslaget fra f�r, s� vi m� sette det inn
				newcnt++;
				swportvlanDupe.put(key, retning);
				outl("[NEW] swportid: " + swportid + " vlan: <b>"+ vlan +"</b> Retning: <b>" + retning + "</b><br>");

				// Sett inn i swportvlan
				String[] fields = {
					"swportid", swportid,
					"vlan", vlan,
					"retning", retning,
				};
				if (DB_UPDATE) Database.insert("swportvlan", fields);
			}

			// S� legger vi til i activeOnTrunk
			//HashMap swrecTrunk;
			HashSet activeVlanOnTrunk;
			if ( (activeVlanOnTrunk = (HashSet)activeOnTrunk.get(swportid)) == null) {
				activeVlanOnTrunk = new HashSet();
				activeOnTrunk.put(swportid, activeVlanOnTrunk);
			}
			activeVlanOnTrunk.add(vlan);
		}

		// N� kan vi g� gjennom swportvlan og slette de innslagene som ikke lenger eksisterer
		Iterator iter = swportvlan.entrySet().iterator();
		while (iter.hasNext()) {
			remcnt++;
			Map.Entry me = (Map.Entry)iter.next();
			String key = (String)me.getKey();
			String swportvlanid = (String)me.getValue();

			StringTokenizer st = new StringTokenizer(key, ":");
			String swportid = st.nextToken();
			String vlan = st.nextToken();

			outl("[REM] swportid: " + swportid + " vlan: <b>"+ vlan +"</b> ("+swportvlanid+")<br>");
			if (DB_UPDATE) Database.update("DELETE FROM swportvlan WHERE swportvlanid = '"+swportvlanid+"'");
		}

		if (newcnt > 0 || updcnt > 0 || remcnt > 0) if (DB_COMMIT) Database.commit();
		outl("New count: <b>"+newcnt+"</b>, Update count: <b>"+updcnt+"</b> Dup count: <b>"+dupcnt+"</b>, Rem count: <b>"+remcnt+"</b><br>");

		if (!DB_COMMIT) Database.rollback();

		// S� skriver vi ut en rapport om mismatch mellom swportallowedvlan og det som faktisk kj�rer
		outl("<h2>Allowed VLANs that are not active:</h2>");
		outl("<h4>(<i><b>Note</b>: VLANs 1 and 1000-1005 are for interswitch control traffic and is always allowed</i>)</h4>");
		int allowedcnt=0, totcnt=0;
		iter = allowedVlan.values().iterator();
		while (iter.hasNext()) {
			HashMap boksAllowedMap = (HashMap)iter.next();
			Iterator iter2 = boksAllowedMap.values().iterator();
			while (iter2.hasNext()) {
				HashMap hm = (HashMap)iter2.next();
				String swportid = (String)hm.get("swportid");
				String hexstring = (String)hm.get("hexstring");

				HashSet activeVlanOnTrunk = (HashSet)activeOnTrunk.get(swportid);
				if (activeVlanOnTrunk == null) {
					//outl("ERROR, swrecTrunk is missing for swportid: " + swportid + "<br>");
					continue;
				}
				totcnt++;

				String boksid = (String)hm.get("boksid");
				String modul = (String)hm.get("modul");
				String port = (String)hm.get("port");
				String boksbak = (String)hm.get("boksbak");
				boolean printMsg = false;

				int startRange=0;
				boolean markLast=false;
				int MIN_VLAN = 2;
				int MAX_VLAN = 999;
				for (int i=MIN_VLAN; i <= MAX_VLAN+1; i++) {
					if (isAllowedVlan(hexstring, i) && !activeVlanOnTrunk.contains(String.valueOf(i)) && i != MAX_VLAN+1 ) {
						if (!markLast) {
							startRange=i;
							markLast = true;
						}
					} else {
						if (markLast) {
							String range = (startRange==i-1) ? String.valueOf(i-1) : startRange+"-"+(i-1);
							if (!printMsg) {
								allowedcnt++;
								outl("Working with trunk From("+boksid+"): <b>"+boksName.get(boksid)+"</b>, Modul: <b>"+modul+"</b> Port: <b>"+port+"</b> To("+boksbak+"): <b>"+boksName.get(boksbak)+"</b><br>");
								// Skriv ut aktive vlan
								out("&nbsp;&nbsp;Active VLANs: <b>");
								Iterator vlanIter = activeVlanOnTrunk.iterator();
								int[] vlanA = new int[activeVlanOnTrunk.size()];
								int vlanAi=0;
								while (vlanIter.hasNext()) vlanA[vlanAi++] = Integer.parseInt((String)vlanIter.next());
								Arrays.sort(vlanA);
								boolean first=true;
								for (vlanAi=0; vlanAi < vlanA.length; vlanAi++) {
									if (!first) out(", "); else first=false;
									out(String.valueOf(vlanA[vlanAi]));
								}
								outl("</b><br>");
								//outl("&nbsp;&nbsp;The following VLANs are allowed on the trunk, but does not seem to be active:<br>");
								printMsg = true;
								out("&nbsp;&nbsp;Excessive VLANs: <b>"+range+"</b>");
							} else {
								out(", <b>"+range+"</b>");
							}
							markLast=false;
						}
						//startRange=i+1;
					}
				}
				if (printMsg) outl("<br><br>");
			}
		}

		outl("A total of <b>"+allowedcnt+"</b> / <b>"+totcnt+"</b> have allowed VLANs that are not active.<br>");
		outl("All done.<br>");
	}

	private boolean vlanTraverseLink(int vlan, String fromid, String boksid, boolean cameFromTrunk, boolean setDirection, HashMap nontrunkVlan, HashMap allowedVlan, HashMap activeVlan, HashSet spanTreeBlocked, ArrayList trunkVlan, HashSet visitNode, int level, Com com, boolean DEBUG_OUT, HashSet boksGwSet, HashSet swportGwVlanSet, HashMap boksName)
	{
		if (level > 40) {
			outl("<font color=\"red\">ERROR! Level is way too big...</font>");
			return false;
		}
		String pad = "";
		for (int i=0; i<level; i++) pad+="        ";

		if (DEBUG_OUT) outl(pad+"><font color=\"green\">[ENTER]</font> Now at node("+boksid+"): <b>" + boksName.get(boksid) + "</b>, came from("+fromid+"): " + boksName.get(fromid) + ", vlan: " + vlan + " cameFromTrunk: <b>"+cameFromTrunk+"</b> level: <b>" + level + "</b>");

		if (visitNode.contains(boksid)) {
			if (DEBUG_OUT) outl(pad+"><font color=\"red\">[RETURN]</font> NOTICE: Found loop, from("+fromid+"): " + boksName.get(fromid) + ", boksid("+boksid+"): " + boksName.get(boksid) + ", vlan: " + vlan + ", level: " + level + "");
			return false;
		}

		// Vi vet n� at dette vlanet kj�rer p� denne boksen, det f�rste vi gj�r da er � traversere videre
		// p� alle ikke-trunker og markerer retningen
		//HashSet foundGwUplinkSet = new HashSet(); // En boks kan kun ha en uplink til samme gw p� et gitt vlan
		boolean isActiveVlan = false;
		if (nontrunkVlan.containsKey(boksid+":"+vlan)) {
			String key = boksid+":"+vlan;
			HashMap nontrunkMap = (HashMap)nontrunkVlan.get(key);

			Iterator iter = nontrunkMap.values().iterator();
			while (iter.hasNext()) {
				HashMap hm = (HashMap)iter.next();
				String toid = (String)hm.get("boksbak");
				String swportid = (String)hm.get("swportid");
				String swportidBack;

				// Linken tilbake skal vi ikke f�lge uansett
				if (toid.equals(fromid)) continue;

				//select swportbak,vlan,boksid,interf from gwport join prefiks using(prefiksid) where swportbak is not null order by swportbak,vlan,boksid,interf;
				//select swportbak,vlan from gwport join prefiks using(prefiksid) where swportbak is not null and vlan is not null order by vlan,swportbak;

				if (boksGwSet.contains(toid)) {
					/*
					if (foundGwUplinkSet.contains(toid)) {
						// Hmm, vi har visst funnet denne f�r, dette kan egentlig ikke skje
						if (DEBUG_OUT) outl(pad+"--><font color=\"red\">[DUP-GW]</font> Error, found two non-trunk uplinks to gw, should not happen. boksid("+boksid+"): " + boksName.get(boksid) + ", to("+toid+"): " + boksName.get(toid) + ", vlan: " + vlan + ", level: <b>" + level + "</b> (<b>"+swportid+"</b>)");
						continue;
					}
					*/
					// Link til GW, vi skal ikke traversere, sjekk om dette vlanet g�r p� denne swporten
					if (swportGwVlanSet.contains(swportid+":"+vlan)) {
						// OK, linken blir da 'o'
						String[] rVlan = {
							swportid,
							String.valueOf(vlan),
							(setDirection)?"o":"u"
						};
						if (DEBUG_OUT) outl(pad+"--><b>[NON-TRUNK-GW]</b> Running on non-trunk, vlan: <b>" + vlan + "</b>, boksid("+boksid+"): <b>" + boksName.get(boksid) + "</b>, to("+toid+"): <b>" + boksName.get(toid) + "</b> level: <b>" + level + "</b> (<b>"+rVlan[0]+"</b>)");
						trunkVlan.add(rVlan);
						isActiveVlan = true;
						//foundGwUplinkSet.add(toid); // Funnet uplink til denne gw'en
					}
					continue;
				}

				// Vi kan n� legge til at retningen skal v�re ned her ihvertfall
				String[] rVlan = {
					swportid,
					String.valueOf(vlan),
					(setDirection)?"n":"u"
				};
				trunkVlan.add(rVlan);
				isActiveVlan = true;

				if (DEBUG_OUT) outl(pad+"--><b>[NON-TRUNK]</b> Running on non-trunk, vlan: <b>" + vlan + "</b>, boksid("+boksid+"): <b>" + boksName.get(boksid) + "</b>, to("+toid+"): <b>" + boksName.get(toid) + "</b> level: <b>" + level + "</b> (<b>"+rVlan[0]+"</b>)");

				// S� traverserer vi linken, return-verdien her er uten betydning
				vlanTraverseLink(vlan, boksid, toid, false, setDirection, nontrunkVlan, allowedVlan, activeVlan, spanTreeBlocked, trunkVlan, visitNode, level+1, com, DEBUG_OUT, boksGwSet, swportGwVlanSet, boksName);

				// S� sjekker vi om vi finner linken tilbake, i s� tilfellet skal den markeres med retning 'o'
				String keyBack = toid+":"+vlan;
				HashMap nontrunkMapBack = (HashMap)nontrunkVlan.get(keyBack);
				if (nontrunkMapBack == null) {
					// Boksen vi ser p� har ingen non-trunk linker, og vi kan derfor g� videre
					if (DEBUG_OUT) outl(pad+"---->ERROR! No non-trunk links found for vlan: " + vlan + ", toid("+toid+"): " + boksName.get(toid) + ", level: " + level + "");
					continue;
				}

				HashMap hmBack = (HashMap)nontrunkMapBack.get(boksid);
				if (hmBack == null) {
					// Linken tilbake mangler
					if (DEBUG_OUT) outl(pad+"---->ERROR! Link back not found for vlan: " + vlan + ", toid("+toid+"): " + boksName.get(toid) + ", level: " + level + "");
					continue;
				}

				swportidBack = (String)hmBack.get("swportid");
				// N� kan vi markere at vlanet kj�rer ogs� p� linken tilbake
				String[] rVlanBack = {
					swportidBack,
					String.valueOf(vlan),
					(setDirection)?"o":"u"
				};
				trunkVlan.add(rVlanBack);
				if (DEBUG_OUT) outl(pad+"--><b>[NON-TRUNK]</b> Link back running on non-trunk OK (<b>"+rVlanBack[0]+"</b>)");
			}
		}

		if (!isActiveVlan) {
			// Ikke aktivt p� noen av portene med boks bak, sjekk om det er
			// aktivt p� noen ikke-trunk porter i det hele tatt
			HashSet hs = (HashSet)activeVlan.get(boksid);
			if (hs != null && hs.contains(new Integer(vlan)) ) {
				isActiveVlan = true;
			}
		}

		// Sjekk om det er noen trunker p� denne enheten vlanet vi er p� har lov til � kj�re p�
		HashMap boksAllowedMap = (HashMap)allowedVlan.get(boksid);
		if (boksAllowedMap == null) {
			if (cameFromTrunk) {
				if (fromid == null) {
					// Dette er f�rste enhet, og da kan dette faktisk skje
					if (DEBUG_OUT) outl(pad+">ERROR! AllowedVlan not found for vlan: " + vlan + ", boksid("+boksid+"): " + boksName.get(boksid) + ", level: " + level + "");
				} else {
					if (DEBUG_OUT) outl(pad+"><font color=\"red\">ERROR! Should not happen, AllowedVlan not found for vlan: " + vlan + ", boksid("+boksid+"): " + boksName.get(boksid) + ", level: " + level + "</font>");
				}
			}
			if (DEBUG_OUT) outl(pad+"><font color=\"red\">[RETURN]</font> from node("+boksid+"): " + boksName.get(boksid) + ", isActiveVlan: <b>" + isActiveVlan+"</b>, no trunks to traverse.");
			// Return true hvis det er noen ikke-trunker som kj�rer p� boksen
			// Dette skal kun ha betydning hvis det er en ikke-trunk opp til gw'en
			return isActiveVlan;
		}
		boolean isActiveVlanTrunk = false;
		Iterator iter = boksAllowedMap.values().iterator();
		while (iter.hasNext()) {
			//HashMap hm = (HashMap)l.get(i);
			HashMap hm = (HashMap)iter.next();
			String hexstr = (String)hm.get("hexstring");
			String toid = (String)hm.get("boksbak");
			String swportid = (String)hm.get("swportid");
			String swportidBack;

			// Linken tilbake skal vi ikke f�lge uansett
			if (toid.equals(fromid)) continue;

			if (boksGwSet.contains(toid)) {
				/*
				if (foundGwUplinkSet.contains(toid)) {
					// Vi har allerede funnet uplink til gw p� en ikke-trunk, og da sier vi at vlanet ikke g�r over denne trunken
					continue;
				}
				*/
				if (swportGwVlanSet.contains(swportid+":"+vlan)) {
					if (!isAllowedVlan(hexstr, vlan)) {
						if (DEBUG_OUT) outl(pad+"--><font color=\"red\">ERROR, running on trunk to GW, but isAllowedVlan is false, vlan: <b>" + vlan + "</b>, boksid("+boksid+"): <b>" + boksName.get(boksid) + "</b>, to("+toid+"): <b>" + boksName.get(toid) + "</b> level: <b>" + level + "</b> (<b>"+swportid+"</b>)");
						continue;
					}

					// Dette er link til en GW, den blir da 'o' og vi skal ikke traversere
					String[] tvlan = {
						swportid,
						String.valueOf(vlan),
						"o"
					};

					if (DEBUG_OUT) outl(pad+"--><b>[TRUNK-GW]</b> Running on trunk, vlan: <b>" + vlan + "</b>, boksid("+boksid+"): <b>" + boksName.get(boksid) + "</b>, to("+toid+"): <b>" + boksName.get(toid) + "</b> level: <b>" + level + "</b> (<b>"+tvlan[0]+"</b>)");
					trunkVlan.add(tvlan);
					isActiveVlanTrunk = true;
				}
				continue;
			}

			// S� trenger vi recorden for linken tilbake
			{
				HashMap boksAllowedMapBack = (HashMap)allowedVlan.get(toid);
				if (boksAllowedMapBack == null) {
					if (DEBUG_OUT) outl(pad+">ERROR! AllowedVlan not found for vlan: " + vlan + ", toid("+toid+"): " + boksName.get(toid) + ", level: " + level + "");
					continue;
				}
				HashMap hmBack = (HashMap)boksAllowedMapBack.get(boksid);
				if (hmBack == null) {
					// Linken tilbake mangler
					if (DEBUG_OUT) outl(pad+"---->ERROR! Link back not found for vlan: " + vlan + ", toid("+toid+"): " + boksName.get(toid) + ", level: " + level + "");
					continue;
				}
				swportidBack = (String)hmBack.get("swportid");

				String hexstrBack = (String)hmBack.get("hexstring");
				// Hvis en av dem ikke tillater dette vlanet � kj�re f�lger vi ikke denne linken
				if (!isAllowedVlan(hexstr, vlan) || !isAllowedVlan(hexstrBack, vlan)) {
					if (DEBUG_OUT) outl(pad+"----><b>NOT</b> allowed to("+toid+"): " + boksName.get(toid) + "");
					continue;
				}

			}

			if (DEBUG_OUT) outl(pad+"----><b>Allowed</b> to("+toid+"): " + boksName.get(toid) + ", visiting...");

			// Sjekk om linken er blokkert av spanning tree
			if (spanTreeBlocked.contains(swportid+":"+vlan) || spanTreeBlocked.contains(swportidBack+":"+vlan)) {
				// Jepp, da legger vi til vlanet med blokking i begge ender
				String[] tvlan = {
					swportid,
					String.valueOf(vlan),
					"b"
				};
				String[] tvlanBack = {
					swportidBack,
					String.valueOf(vlan),
					"b"
				};
				trunkVlan.add(tvlan);
				trunkVlan.add(tvlanBack);
				isActiveVlanTrunk = true;
				if (DEBUG_OUT) outl(pad+"------><font color=\"purple\">Link blocked by spanning tree, boksid("+boksid+"): <b>"+boksName.get(boksid)+"</b> toid:("+toid+"): <b>"+ boksName.get(toid) + "</b>, vlan: <b>" + vlan + "</b>, level: <b>" + level + "</b></font>");
				continue;
			}


			//if (DEBUG_OUT) outl(pad+"---->Visiting("+toid+"): " + boksName.get(toid) + "");

			// Brukes for � unng� dupes
			visitNode.add(boksid);

			if (vlanTraverseLink(vlan, boksid, toid, true, setDirection, nontrunkVlan, allowedVlan, activeVlan, spanTreeBlocked, trunkVlan, visitNode, level+1, com, DEBUG_OUT, boksGwSet, swportGwVlanSet, boksName)) {
				// Vi vet n� at vlanet kj�rer p� denne trunken
				String[] tvlan = {
					swportid,
					String.valueOf(vlan),
					(setDirection)?"n":"u"
				};
				String[] tvlanBack = {
					swportidBack,
					String.valueOf(vlan),
					(setDirection)?"o":"u"
				};
				trunkVlan.add(tvlan);
				trunkVlan.add(tvlanBack);
				isActiveVlanTrunk = true;
				if (DEBUG_OUT) outl(pad+"---->Returned active on trunk, vlan: <b>" + vlan + "</b>, boksid("+boksid+"): <b>" + boksName.get(boksid) + "</b>, to("+toid+"): <b>" + boksName.get(toid) + "</b> level: <b>" + level + "</b> (<b>"+tvlan[0]+" '"+tvlan[2]+"' / "+tvlanBack[0]+" '"+tvlanBack[2]+"'</b>)");
			} else {
				if (DEBUG_OUT) outl(pad+"---->Returned NOT active on trunk, vlan: <b>" + vlan + "</b>, boksid("+boksid+"): <b>" + boksName.get(boksid) + "</b>, to("+toid+"): <b>" + boksName.get(toid) + "</b> level: <b>" + level + "</b>");
			}
			visitNode.remove(boksid);


		}


		// Vi skal returnere om vlanet kj�rer p� denne boksen
		// F�rst sjekker vi om noen av trunkene har dette vlanet aktivt
		if (isActiveVlanTrunk) {
			if (DEBUG_OUT) outl(pad+"><font color=\"red\">[RETURN]</font> from node("+boksid+"): " + boksName.get(boksid) + ", <b>ActiveVlan on trunk</b>");
			return true;
		}

		// Nei, da sjekker vi om det er noen ikke-trunker som har det aktivt
		if (isActiveVlan) {
			if (DEBUG_OUT) outl(pad+"><font color=\"red\">[RETURN]</font> from node("+boksid+"): " + boksName.get(boksid) + ", <b>ActiveVlan on NON-trunk</b>");
			return true;
		}

		/*
		HashSet hs = (HashSet)activeVlan.get(boksid);
		if (hs != null && hs.contains(new Integer(vlan)) ) {
			if (DEBUG_OUT) outl(pad+"><font color=\"red\">[RETURN]</font> from node("+boksid+"): " + boksName.get(boksid) + ", <b>ActiveVlan on NON-trunk</b>");
			return true;
		}
		*/
		if (DEBUG_OUT) outl(pad+"><font color=\"red\">[RETURN]</font> from node("+boksid+"): " + boksName.get(boksid) + ", <b>Not active</b>");
		return false;
	}

	private static boolean isAllowedVlan(String hexstr, int vlan)
	{
		if (hexstr.length() == 256) {
			return isAllowedVlanFwd(hexstr, vlan);
		}
		return isAllowedVlanRev(hexstr, vlan);
	}

	private static boolean isAllowedVlanFwd(String hexstr, int vlan)
	{
		if (vlan < 0 || vlan > 1023) return false;
		int index = vlan / 4;

		int allowed = Integer.parseInt(String.valueOf(hexstr.charAt(index)), 16);
		return ((allowed & (1<<3-(vlan%4))) != 0);
	}

	private static boolean isAllowedVlanRev(String hexstr, int vlan)
	{
		if (vlan < 0 || vlan > 1023) return false;
		int index = hexstr.length() - (vlan / 4 + 1);
		if (index < 0) return false;

		int allowed = Integer.parseInt(String.valueOf(hexstr.charAt(index)), 16);
		return ((allowed & (1<<(vlan%4))) != 0);
	}



	private void outl(String s)
	{
		if (com == null) {
			System.out.println(s);
		} else {
			com.outl(s);
		}
	}
	private void out(String s)
	{
		if (com == null) {
			System.out.print(s);
		} else {
			com.out(s);
		}
	}
}