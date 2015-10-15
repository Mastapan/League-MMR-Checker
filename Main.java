package com.company;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constant.Season;
import dto.League.League;
import dto.League.LeagueEntry;
import dto.Match.MatchDetail;
import dto.Match.Participant;
import dto.Match.ParticipantIdentity;
import dto.MatchList.MatchList;
import dto.MatchList.MatchReference;
import dto.Summoner.Summoner;import constant.Region;

import main.java.riotapi.RiotApi;
import main.java.riotapi.RiotApiException;
import java.util.Scanner;

public class Main {
    public static RiotApi api = new RiotApi("00e82d10-743e-4adf-a206-2c967817a311");
    public static HashMap<String, Integer> elomap = new HashMap<String, Integer>();
    public static String[] Tiers = {"BRONZE","SILVER","GOLD","PLATINUM","DIAMOND"};
    public static String[] Divisions = {"V","IV","III","II","I"};
    public static String summonerName;







    public  static void main(String[] args) throws RiotApiException {

            int starting = 800;
            for(int i = 0; i <Tiers.length; i++){
                for(int j = 0; j <Divisions.length ; j++){
                    elomap.put(Tiers[i]+Divisions[j],starting);
                    starting += 70;
                }
            }

//TODO filter ranked solo, account for baddies not in leagues , weight by recent, make code less shitty,(prolly fixed) for some reason if i retrieve 5 games it only gives me 2 wtf?



        Scanner findUsername=new Scanner(System.in);
        System.out.print("What is your summoner name?:");
        summonerName= findUsername.next();

        int output = calculate(elochart(playeridlist(matchidlist(summonerid(summonerName)),summonerid(summonerName)),matchidlist(summonerid(summonerName))));
        System.out.println("lel");
        System.out.println(output);
        System.out.println("rip");

    }
    public static long summonerid(String summonerName) throws RiotApiException{
        Map<String, Summoner> summoners = api.getSummonersByName(Region.NA, "kumoriyuki");
        Summoner summoner = summoners.get("kumoriyuki");
        System.out.println(summoner.getName());
        long value = summoner.getId();
        return value;

    }
    public static long[] matchidlist(long id) throws RiotApiException{
        //sets stuff up




        List<MatchReference> matchinfo = api.getMatchList(id).getMatches();
        //filter by ranked 5v5 i guess,
        List<MatchReference> shortened = matchinfo.subList(0, 10);
        long[] matchidlist = new long[shortened.size()];
        for(int i=0; i<shortened.size(); i ++){
            matchidlist[i] =  shortened.get(i).getMatchId();

        }
        return matchidlist;

    }
    public static Map<Long, List<Long>> playeridlist(long[] matchid,long id) throws RiotApiException{
        //Makes list of past x matches and players assocaiated with each match
        Map<Long, List<Long>> playeridlist = new HashMap<Long, List<Long>>();
        //long[][] playeridlist = new long[matchid.length][];
        for (int i = 0; i < matchid.length; i++) {
            MatchDetail match = api.getMatch(matchid[i]);
            List<ParticipantIdentity> hi = match.getParticipantIdentities();
            List<Long> templist = new ArrayList<Long>();

            for(int j = 0, k =0 ; k < hi.size(); j ++,k++) {
                if((hi.get(j).getPlayer().getSummonerId() == id)){
                    k++;
                }

                if (!(hi.get(j).getPlayer().getSummonerId() == id)) {
                    //not working

                    templist.add(hi.get(k).getPlayer().getSummonerId());

                }

            }
            playeridlist.put(matchid[i],templist);
            try{
                Thread.sleep(2000);

            } catch(InterruptedException ex){
                Thread.currentThread().interrupt();
            }
            System.out.print("wat");


        }
        return playeridlist;

    }
    public static ArrayList<Integer> elochart(Map<Long,List<Long>> playeridlist,long[] matchid) throws RiotApiException{

//        for(int i = 0; i < playeridlist.length; i++){
//            for(int j = 0 ; j< playeridlist[i].length; j++) {
//                System.out.println(playeridlist[i][j]);
//            }
//
//        }




        //Returns the "elo" of players using summoner id


        List<List<List<League>>> league = new ArrayList<List<List<League>>>();

        for(int i = 0; i < matchid.length; i++){
            List<List<League>> leaguesub = new ArrayList<List<League>>();
            for(int j = 0 ; j< playeridlist.get(matchid[i]).size(); j++) {
                System.out.println(playeridlist.get(matchid[i]).get(j));
                //something something not in a league
                leaguesub.add(api.getLeagueEntryBySummoner(Region.NA, playeridlist.get(matchid[i]).get(j)));
                try{
                    Thread.sleep(2000);

                } catch(InterruptedException ex){
                    Thread.currentThread().interrupt();
                }

                }
            league.add(leaguesub);

                }
        ArrayList<Integer> elolist = new ArrayList<Integer>();
        System.out.println("wtf is league" + league.size());
        for(List<List<League>> hi : league) {


            for (List<League> z : hi) {
                //check prolly
            //List<League> z = hi.get(0);
            System.out.println("number of games " + z.size());
               // for (League s : z) {
                League s = z.get(0);

                    String tier = s.getTier();
                    for (LeagueEntry y : s.getEntries()) {
                        int leaguepoints = y.getLeaguePoints();
                        String division = y.getDivision();
                        elolist.add(mmr(tier, division , leaguepoints));


                    }
                //}
           }
        }
        System.out.println(elolist.size() + "wtf why has it shrunk");
            return elolist;



        }
    public static int calculate(ArrayList<Integer> elolist){
        int sum = 0;
        for(int i = 0; i<elolist.size(); i++){
            System.out.println(elolist.get(i));
           sum += elolist.get(i);
        }

        return sum/elolist.size();

    }







    public static int mmr(String tier, String Division, int leaguepoints) {
        return elomap.get(tier+Division)+(70*leaguepoints/100); //check int div
    }

}
