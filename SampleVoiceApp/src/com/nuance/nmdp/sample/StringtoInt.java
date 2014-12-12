package com.nuance.nmdp.sample;

import java.util.StringTokenizer;

public class StringtoInt {
	public static int inNumerals(String inwords)
	{	
	    int wordnum = 0;
	    inwords=inwords.toLowerCase();
	    String[] arrinwords = inwords.split(" ");
	    int arrinwordsLength = arrinwords.length;
	    if(inwords.equals("zero"))
	    {
	        return 0;
	    }
	    if(inwords.contains("thousand"))
	    {
	        int indexofthousand = inwords.indexOf("thousand");
	        //System.out.println(indexofthousand);
	        String beforethousand = inwords.substring(0,indexofthousand);
	        //System.out.println(beforethousand);
	        String[] arrbeforethousand = beforethousand.split(" ");
	        int arrbeforethousandLength = arrbeforethousand.length;
	        //System.out.println(arrbeforethousandLength);
	        if(arrbeforethousandLength==2)
	        {
	            wordnum = wordnum + 1000*(wordtonum(arrbeforethousand[0]) + wordtonum(arrbeforethousand[1]));
	            //System.out.println(wordnum);
	        }
	        if(arrbeforethousandLength==1)
	        {
	            wordnum = wordnum + 1000*(wordtonum(arrbeforethousand[0]));
	            //System.out.println(wordnum);
	        }

	    }
	    if(inwords.contains("hundred"))
	    {
	        int indexofhundred = inwords.indexOf("hundred");
	        //System.out.println(indexofhundred);
	        String beforehundred = inwords.substring(0,indexofhundred);

	        //System.out.println(beforehundred);
	        String[] arrbeforehundred = beforehundred.split(" ");
	        int arrbeforehundredLength = arrbeforehundred.length;
	        wordnum = wordnum + 100*(wordtonum(arrbeforehundred[arrbeforehundredLength-1]));
	        String afterhundred = inwords.substring(indexofhundred+8);//7 for 7 char of hundred and 1 space
	        //System.out.println(afterhundred);
	        String[] arrafterhundred = afterhundred.split(" ");
	        int arrafterhundredLength = arrafterhundred.length;
	        if(arrafterhundredLength==1)
	        {
	            wordnum = wordnum + (wordtonum(arrafterhundred[0]));
	        }
	        if(arrafterhundredLength==2)
	        {
	            wordnum = wordnum + (wordtonum(arrafterhundred[1]) + wordtonum(arrafterhundred[0]));
	        }
	        //System.out.println(wordnum);

	    }
	    if(!inwords.contains("thousand") && !inwords.contains("hundred"))
	    {
	        if(arrinwordsLength==1)
	        {
	            wordnum = wordnum + (wordtonum(arrinwords[0]));
	        }
	        if(arrinwordsLength==2)
	        {
	            wordnum = wordnum + (wordtonum(arrinwords[1]) + wordtonum(arrinwords[0]));
	        }
	        //System.out.println(wordnum);
	    }


	    return wordnum;
	}   


	public static int wordtonum(String word)
	{
	        int num = 0;
	        switch (word) {
	            case "one":  num = 1;
	                     break;
	            case "two":  num = 2;
	                     break;
	            case "three":  num = 3;
	                     break;
	            case "four":  num = 4;
	                     break;
	            case "five":  num = 5;
	                     break;
	            case "six":  num = 6;
	                     break;
	            case "seven":  num = 7;
	                     break;
	            case "eight":  num = 8;
	                     break;
	            case "nine":  num = 9;
	                     break;
	            case "ten": num = 10;
	                     break;
	            case "eleven": num = 11;
	                     break;
	            case "twelve": num = 12;
	                     break;
	            case "thirteen": num = 13;
	                     break;
	            case "fourteen": num = 14;
	                     break;             
	            case "fifteen": num = 15;
	                     break;
	            case "sixteen": num = 16;
	                     break;
	            case "seventeen": num = 17;
	                     break;
	            case "eighteen": num = 18;
	                     break;
	            case "nineteen": num = 19;
	                     break;
	            case "twenty":  num = 20;
	                     break;
	            case "thirty":  num = 30;
	                     break;
	            case "forty":  num = 40;
	                     break;
	            case "fifty":  num = 50;
	                     break;
	            case "sixty":  num = 60;
	                     break;
	            case "seventy":  num = 70;
	                     break;
	            case"eighty":  num = 80;
	                     break;
	            case "ninety":  num = 90;
	                     break; 
	            case "hundred": num = 100;
	                        break;
	            case "thousand": num = 1000;
	                        break;     
	           /*default: num = "Invalid month";
	                             break;*/
	        }
	        return num;
	    }

}
