import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;


public class AddressDatabase {
	//The database
	HashMap<String, HashMap<String, Float>> map = new HashMap<String, HashMap<String, Float>>();

	//A database/copy of all the traces that were run in this instance of the application,
	//including the loaded traces.
	public ArrayList<ArrayList<String>> traces = new ArrayList<ArrayList<String>>(0);
	int traceCount = 0;

	public String saveIt() {
		String copy = "";
		for (int x =0; x < traces.size(); x++) {
			for (String s : traces.get(x)) {
				copy = copy + s + "/line";
			}
			copy = copy + "/trace";
		}
		return copy;
	}

	public void loadIt_Append(String copy) {
		String[] aTraces = copy.split("/trace");

		for (String trace : aTraces) {
			ArrayList<Float> pathHop = new ArrayList<Float>(0);
			ArrayList<String> pathIP = new ArrayList<String>(0);

			//It may be a problem if the for loop does not run after
			//this line, the array-list will still have an extra entry.
			traces.add(new ArrayList());

			String[] aLine = trace.split("/line");

			for (String line : aLine){
				traces.get(traceCount).add(line);
				String hopp = line.substring(0, 4);
				Float hop = Float.parseFloat(hopp);
				pathHop.add(hop);

				String ip = line.substring(32);
				ip = ip.trim();
				pathIP.add(ip); 
			}
			traceCount = traceCount + 1;

			int sCount = 0;
			int dCount = 1;
			while ((dCount < pathHop.size())) {
				Float tHop = pathHop.get(dCount) - pathHop.get(sCount);
				mapIt(pathIP.get(sCount), pathIP.get(dCount), (tHop - 1));                    
				sCount++;
				dCount++;
			}

		}

	}

	public void trace(String ttl, String  wait, String target)throws UnknownHostException, IOException{

		ArrayList<Float> pathHop = new ArrayList<Float>(0);
		ArrayList<String> pathIP = new ArrayList<String>(0);
		Runtime r = Runtime.getRuntime();
		Process p = r.exec("tracert -d -h " + ttl + " -w " + wait + " " + target);

		BufferedReader buff = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;

		//It may be a problem if the for loop does not run after
		//this line, the array-list will still have an extra entry.
		traces.add(new ArrayList());

		for (int x = 0; (line = buff.readLine()) != null; x++) {
			if ((x > 3) && (line.length() < 49) && (line.length() > 20)){
				traces.get(traceCount).add(line);
				String hopp = line.substring(0, 4);
				Float hop = Float.parseFloat(hopp);
				pathHop.add(hop);

				String ip = line.substring(32);
				ip = ip.trim();
				pathIP.add(ip);
			}
		}
		traceCount = traceCount + 1;

		int sCount = 0;
		int dCount = 1;
		while ((dCount < pathHop.size())) {
			Float tHop = pathHop.get(dCount) - pathHop.get(sCount);
			mapIt(pathIP.get(sCount), pathIP.get(dCount), (tHop - 1));                    
			sCount++;
			dCount++;
		}
	}

	//The alogrithm to develop the database
	private void mapIt(String src, String dest,Float hc) {
		boolean update = false;
		if (map.containsKey(src)) {

			if (map.get(src).containsKey(dest)) {
				if (map.get(src).get(dest) > hc) { 
					map.get(src).put(dest, hc);
					update = true;
				} else {
					//Break out of mapIt()
					return;
			}               
			} else {
				map.get(src).put(dest, hc);
			}

			if (map.containsKey(dest)) {
				if (map.get(dest).containsKey(src)) {
					if (update) { 
						map.get(dest).put(src, hc);
					} 
				} else {
					map.get(dest).put(src, hc);
				}
			} else {
				map.put(dest, new HashMap<String, Float>());
				map.get(dest).put(src, hc);
			}

		} else {

			map.put(src, new HashMap<String, Float>());
			map.get(src).put(dest, hc);
			if (map.containsKey(dest)) {
				if (map.get(dest).containsKey(src)) {
					if (update) { 
						map.get(dest).put(src, hc);
					} 
				} else {
					map.get(dest).put(src, hc);
				}
			} else {
				map.put(dest, new HashMap<String, Float>());
				map.get(dest).put(src, hc);
			}

		}
	}
}