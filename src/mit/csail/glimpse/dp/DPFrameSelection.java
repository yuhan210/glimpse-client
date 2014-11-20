package mit.csail.glimpse.dp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DPFrameSelection {

	public static List<Integer> run(List<Integer> diffs, int l, int P){
		int[][] M = new int[500][500];
		int[][] solution = new int[500][500];
		int dp_n = diffs.size();
		List<Integer> dp_ind = new ArrayList<Integer>();
		
		// Init
		for (int i = 0; i < dp_n; ++i){
			int value = 0;			
			if ((i-1) >= 0) {
				value = M[i-1][0];
			}			
			M[i][0] = diffs.get(i) + value;	
		}
		
		for (int i = 0; i < l; ++i){
			M[0][i] = diffs.get(0);
		}
		
		// DP
		for (int i = 1; i < dp_n; ++i){ // element
			for (int j = 1; j < l; ++j){ //partition
				int best = Integer.MAX_VALUE;
				int min_index = 1;
				
				for (int x = 0; x < i; ++x){
					int cost = Max(M[x][j-1], M[i][0] - M[x][0]);			
					if(cost < best) {
						best = cost;
						min_index = x;
					}
				}			
				M[i][j] = best;
				solution[i-1][j-1] = min_index;
			}
		}
			
		
		// Backtracking
		List<List<Integer>> reversed_answer = new ArrayList<List<Integer>>();	
		int k = l - 2;
		dp_n = diffs.size() - 1;
		while( k >= 0 ){
			List<Integer> list = new ArrayList<Integer>();
			for(int i = solution[dp_n-1][k]+1; i < dp_n+1; ++i){
				list.add(diffs.get(i));				
			}		
			reversed_answer.add(list);			
			dp_n = solution[dp_n-1][k];
			k = k-1;
		}
		
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < dp_n+1; ++i){
			list.add(diffs.get(i));
		}
		reversed_answer.add(list);
				
		
		List<List<Integer>> result = new ArrayList<List<Integer>>();	
		for(int i = reversed_answer.size()-1; i >= 0; --i ){
			result.add(reversed_answer.get(i));
		}		
		
		int ind_counter = 0;
		for(int i = 0; i < result.size(); ++i){
			dp_ind.add(ind_counter);			
			ind_counter += result.get(i).size();			
		}
		
		
		dp_ind.add(ind_counter);
		
		if (!findElement(dp_ind, 0))
			dp_ind.add(0);
		
		
		if (!findElement(dp_ind, P-1))
			dp_ind.add(P-1);
				
		Collections.sort(dp_ind);
				
		/**
		for(int i = 0; i < dp_ind.size(); ++i){
			if (i == 0)
				System.out.println(dp_ind.get(i));
			else
				System.out.println("," + dp_ind.get(i));
		}	
		**/
		return dp_ind;
	}
	
	static Boolean findElement(List<Integer> l, int e){
		for(int i = 0; i < l.size(); ++i){
			if (l.get(i) == e){				
				return true;
			}
		}
		return false;
	}
	
	static int Max(int a, int b){
		if (a > b)
			return a;
		return b;
	}
}
