package matchup.g5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.lang.*;

import matchup.sim.utils.*;

public class Player implements matchup.sim.Player {
	private List<Integer> skills;
	private List<List<Integer>> distribution;
	private List<Integer> availableRows;
	private Random rand;
  private List<Integer> opponentSkills;
  private List<List<Integer>> opponentDistribution;

  /* helper variable to pass back results from permutation */
  private List<Integer> permute_result;
  private int best_permuted_score_cur_line;

  /* history variable */
  private List<Game> games;

  private boolean isHome;
  private String prevGameResult;
	private String prevStrategy; //variable to store strategy used in previous game

    /* created once for repeated games */
	public Player() {
		rand = new Random();
		skills = new ArrayList<Integer>();
		distribution = new ArrayList<List<Integer>>();
		availableRows = new ArrayList<Integer>();
        opponentSkills = new ArrayList<Integer>();
        opponentDistribution = new ArrayList<List<Integer>>();
        games = new ArrayList<Game>();
        isHome = true; // default
        permute_result = new ArrayList<Integer>();
        best_permuted_score_cur_line = -6;
        prevGameResult = "t";

		for (int i=0; i<3; ++i) availableRows.add(i);
	}

    public void init(String opponent) {
    }

    /* called once per game repeat (pair of home/away) */
	public List<Integer> getSkills() {
				//Aggressive strategy
				List<Integer> aggressive = new ArrayList<Integer>();
				for(int i=0; i<5; i++){
					aggressive.add(9);
					aggressive.add(7);
				}
				for(int i=0; i<3; i++){
					aggressive.add(1);
				}
					aggressive.add(4);
					aggressive.add(3);

				//Defensive strategy
				List<Integer> defensive = new ArrayList<Integer>();
				for(int i=0; i<10; i++){
					defensive.add(7);
				}
				for(int i=0; i<5; i++){
					defensive.add(4);
				}

				// Mixed strategy - counters a lineup of 9s, 8s and 1s
				List<Integer> mixed = new ArrayList<Integer>();
				for(int i=0; i<5; i++){
					mixed.add(8);
					mixed.add(6);
					mixed.add(4);
				}

				//Uses the trueRandom algorithm to generate a balanced random
				//lineup with skills between 4 and 9
				List<Integer> semiRand = trueRandom(4,9,90,15);

        /* obtain and analyze game history */
        games = History.getHistory();

        int ourScore = 0;
        int oppScore = 0;

				//Determine total score of the previous game (adds scores from home and away)
				if(games.size() >= 2){
					for (int i = games.size() - 2; i < games.size(); i++) {
	            if(i < 0) continue;
	            if(games.get(i).playerA.name.equals("g5")) {
	                ourScore = ourScore + games.get(i).playerA.score;
	            } else {
	                oppScore = oppScore + games.get(i).playerA.score;
	            }
	            if(games.get(i).playerB.name.equals("g5")) {
	                ourScore = ourScore + games.get(i).playerB.score;
	            } else {
	                oppScore = oppScore + games.get(i).playerB.score;
	            }
	        }
				}


        if(ourScore < oppScore){
					prevGameResult = "l";
        }
				else if(ourScore == oppScore){
					prevGameResult = "t";
        }
				else{
					prevGameResult = "w";
				}

				// Test statement
        //System.out.println("prevGameResult = " + prevGameResult);

				// Choose which strategy to play based on last game

		    this.skills = defensive;
            return this.skills;
        }

	// This algorithm will select 'num' random integers from the range [min, max] that add up to the desired 'sum'.
	// It isn't hard coded to select 15 random numbers adding up to 90, and can be used to adaptively select a team
	// by changing the range or manually selecting a few players and having the algorithm fill out the rest
	public static List<Integer> trueRandom(int min, int max, int sum, int num){
    Random r = new Random();
		int desired_sum = sum;
		int current_sum = 0;
		int remaining = desired_sum - current_sum;
		int current_min = min;
		int current_max = max;
		int num_players = num;
		int player_skill = 0;
    List<Integer> randSkills = new ArrayList<Integer>();

    for(int i=0; i<num; i++){
      num_players--;
      if(num_players != 0){
        while (((remaining - current_max)/num_players) <= min){
          current_max -= 1;
        }
        while (((remaining - current_min)/num_players) >= max){
          current_min += 1;
        }

        int range = current_max - current_min;
        //System.out.println("The range is: (" + current_min + ", " + current_max + ")");
        if(range <= 0){
          player_skill = current_max;
        }
        else{
          player_skill = current_min + r.nextInt(range);
        }
        randSkills.add(player_skill);
        current_sum += player_skill;
        remaining = desired_sum - current_sum;
      }
      else{
        player_skill = remaining;
        randSkills.add(player_skill);
        current_sum += player_skill;
        remaining = desired_sum - current_sum;
      }
    }
    return randSkills;
  }

    /* called every home/away switch */
    public List<List<Integer>> getDistribution(List<Integer> opponentSkills, boolean isHome) {
        //System.out.println("last distribution: ");
        //System.out.println(distribution);
    	distribution = new ArrayList<List<Integer>>();
        // If we're the home team, create variance in our line
        if (isHome) {


        }
    	List<Integer> index = new ArrayList<Integer>();
    	for (int i=0; i<15; ++i) index.add(i);


		Collections.shuffle(index);
		int n = 0;
    	for (int i=0; i<3; ++i) {
    		List<Integer> row = new ArrayList<Integer>();
    		for (int j=0; j<5; ++j) {
    			row.add(skills.get(index.get(n)));
    			++n;
    		}

    		distribution.add(row);
    	}

        // update our private variables
        this.isHome = isHome;
        this.opponentSkills = opponentSkills;


    	return distribution;
    }


    /* called every round of play
     * when away, opponentRound is historical data
     */
    public List<Integer> playRound(List<Integer> opponentRound) {
        /* initialize return variable */
        List<Integer> round = null;

        /* log opponent data */
        opponentDistribution.add(opponentRound);

        /* print tests */
        //System.out.println(isHome);

        /* permutation when isHome = True */
        if (isHome == true) {
            int selected_line_score = -6;
            int selected_line_index = 0; // default first line, will be overwritten
            for (int i = 0; i < availableRows.size(); i++) {

                /* TEST */
                //System.out.println("--------------------------------------------------------------------");
                //System.out.println("Line permuting currently: " + distribution.get(availableRows.get(i)));
                //System.out.println("--------------------------------------------------------------------");
                /* TEST END */
                /* clear the return variables */
                best_permuted_score_cur_line = -6; // resets best_permuted_score_cur_line for each line permutation
                permute_result = null;

                line_permute(distribution.get(availableRows.get(i)), opponentRound);

                if (best_permuted_score_cur_line > selected_line_score) {
                    /* test */
                    //System.out.println("1.!!!!!!!!!!!!!!!!!!");

                    selected_line_score = best_permuted_score_cur_line;
                    selected_line_index = i;
                    round = permute_result;
                } else if (best_permuted_score_cur_line == selected_line_score) {
                    int selected_line_skill_sum = 0;
                    int current_line_skill_sum = 0;
                    for (int j = 0; j < 5; j++) {
                        selected_line_skill_sum = selected_line_skill_sum + round.get(j);
                        current_line_skill_sum = current_line_skill_sum + permute_result.get(j);
                    }
                    if (current_line_skill_sum < selected_line_skill_sum) {
                        /* test */
                        //System.out.println("2.!!!!!!!!!!!!!!!!!!");

                        selected_line_score = best_permuted_score_cur_line;
                        selected_line_index = i;
                        round = permute_result;
                    }
                } else {}

                /* test */
                //System.out.println("test: Best permutation of the line: " + permute_result);
                //System.out.println("test: Resulting net Score of best permutation: " + best_permuted_score_cur_line);
            }
            availableRows.remove(selected_line_index);

            //System.out.println("Selected Line: " + round);
            //System.out.println("Resulting net Score: " + selected_line_score);

        } else {

            /* random fillers */
            int n = rand.nextInt(availableRows.size());
            round = new ArrayList<Integer>(distribution.get(availableRows.get(n)));
            availableRows.remove(n);
            Collections.shuffle(round);
        }

    	return round;
    }

    /* permutation function
     * returns ourPoint - opponentPoint under our best permutation
     * permutation algorithm is based on:
     * https://www.geeksforgeeks.org/write-a-c-program-to-print-all-permutations-of-a-given-string/
     */
    private int line_permute(List<Integer> ourTeam, List<Integer> opponent) {
        /*
         * l = starting index of the string
         * r = ending index of the string
         */
        int l = 0;
        int r = ourTeam.size() - 1;
        permute(ourTeam, l, r, opponent);
        return 0;
    }

    private void permute(List<Integer> ourTeam, int l, int r, List<Integer> opponent) {
        if (l == r) {
            //System.out.print(ourTeam);
            int cur_score = 0;
            for (int i = 0; i < ourTeam.size(); i++) {
                if(ourTeam.get(i) - opponent.get(i) >= 3) {
                    cur_score++;
                } else if (opponent.get(i) - ourTeam.get(i) >= 3) {
                    cur_score--;
                } else {}
                //System.out.println(cur_score); // test
            }
            if (cur_score > best_permuted_score_cur_line) {
                /* test */
                //System.out.println("!?!?!?!?!?!?!?");

                best_permuted_score_cur_line = cur_score;
                permute_result = new ArrayList<Integer>(ourTeam);
            }
            //System.out.println("permute_result: " + permute_result);
            //System.out.println(" : best perm. score = " + cur_score);

        } else {
            for (int i = l; i <= r; i++) {
                /* SWAP */
                int temp = ourTeam.get(l);
                ourTeam.add(l, ourTeam.get(i));
                ourTeam.remove(l + 1);
                ourTeam.add(i, temp);
                ourTeam.remove(i + 1);


                permute(ourTeam, l + 1, r, opponent);

                /* SWAP */
                temp = ourTeam.get(l);
                ourTeam.add(l, ourTeam.get(i));
                ourTeam.remove(l + 1);
                ourTeam.add(i, temp);
                ourTeam.remove(i + 1);
            }
        }
    }


    public void clear() {
    	availableRows.clear();
    	for (int i=0; i<3; ++i) availableRows.add(i);
    }
}
