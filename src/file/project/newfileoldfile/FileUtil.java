package file.project.newfileoldfile;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: xueYaDong
 * @Company: 东方标准
 * @Date: 2019/12/02/12:50
 * @Description:
 */
public class FileUtil {

    static Map<String,Map> resultMap=new HashMap();
    public static void main(String[] args) {
        resultMap.clear();
        String path1="E://comparetest/2.txt";//旧文本
        String path2="E://comparetest/3.txt";//新文本
        Map<Integer,String> oldLines=readFile(path1);
        Map<Integer,String> newLines=readFile(path2);

        int total=0;
        compare(oldLines,newLines, total);

        //展示结果
        System.out.println();
        System.out.println("对比结果展示：");
        System.out.println();
        for(String changeType:resultMap.keySet()){
            if(changeType.contains("add")) {
                Map<Integer,String> addLines=resultMap.get(changeType);
                for(Integer linesNum:addLines.keySet()) {
                    System.out.println("新文本中新增了第"+(linesNum+1)+"行，内容为：“"+addLines.get(linesNum)+"”");
                }
            }



            if(changeType.contains("delete")) {
                Map<Integer,String> delLines=resultMap.get(changeType);
                for(Integer linesNum:delLines.keySet()) {
                    System.out.println("旧文本中删除了第"+(linesNum+1)+"行，内容为：“"+delLines.get(linesNum)+"”");
                }
            }

            if(changeType.contains("update")) {
                Map<Integer,Integer> updateLines=resultMap.get(changeType);
                for(Integer linesNum:updateLines.keySet()) {
                    System.out.println("旧文本中的第"+(linesNum+1)+"行，内容为：“"+oldLines.get(linesNum)
                            +"”，被修改为新文本中的第"+(updateLines.get(linesNum)+1)+"行，内容为“"+newLines.get(updateLines.get(linesNum))+"”");
                }
            }

        }


    }
    //准备方法，循环比对文本
    public static void compare(Map<Integer,String> oldLines,Map<Integer,String> newLines,int total) {

        Map breakPoint=getBreakPoint(oldLines,newLines);
        if(breakPoint!=null) {
            int oldStart=(int) breakPoint.get("oldLinesBreakStart");
            int newStart=(int) breakPoint.get("newLinesBreakStart");

            //将从差异区域起始点点之后全部的行存入新的集合，以便后面再寻找差异区域的结束位置
            Map<Integer, String> oldLeftLines=new HashMap();
            Map<Integer, String> newLeftLines=new HashMap();

            for(Integer oldLinesNum:oldLines.keySet()) {
                if(oldLinesNum>=oldStart) {

                    oldLeftLines.put(oldLinesNum, oldLines.get(oldLinesNum));
                }
            }
            for(Integer newLinesNum:newLines.keySet()) {
                if(newLinesNum>=newStart) {
                    newLeftLines.put(newLinesNum, newLines.get(newLinesNum));
                }
            }


            int newLinesStart=0;

            Map reConnPoint=new HashMap();
            //调用方法，寻找差异区域的终点位置
            reConnPoint=getConn(oldLeftLines,newLeftLines,newLinesStart,reConnPoint);
            //如果找到了终点位置
            if(reConnPoint.get("oldLinesConnPoint")!=null) {
                int oldEnd=(int) reConnPoint.get("oldLinesConnPoint");
                int newEnd=(int) reConnPoint.get("newLinesConnPoint");
                //调用方法，分析差异区域的变动类型
                analType(newStart,newEnd,oldStart,oldEnd,newLines,oldLines,total);

                //取出新旧文本中剩余行的集合，准备使用递归进行新一轮的寻找差异区域……
                Map<Integer,String> nextOldLines=new HashMap();
                Map<Integer,String> nextNewLines=new HashMap();
                for(int oldLinseNum:oldLines.keySet()) {
                    if(oldLinseNum>=oldEnd) {
                        nextOldLines.put(oldLinseNum, oldLines.get(oldLinseNum));
                    }
                }

                for(int newLinesNum:newLines.keySet()) {
                    if(newLinesNum>=newEnd) {
                        nextNewLines.put(newLinesNum, newLines.get(newLinesNum));
                    }
                }
                total++;
                //递归执行本方法，相当于我们的大脑继续向下读文本内容，寻找新的差异区域
                compare(nextOldLines,nextNewLines,total);
                //如果差异区域没有终点，说明用户是在文件的最后修改的，为了程序计算，新增一个虚拟的行表示终点
            }else {

                Object[] oldLineNums = oldLines.keySet().toArray();
                Arrays.sort(oldLineNums);
                int oldEnd=Integer.valueOf(oldLineNums[oldLineNums.length-1].toString())+1;

                Object[] newLineNums = newLines.keySet().toArray();
                Arrays.sort(newLineNums);
                int newEnd=Integer.valueOf(newLineNums[newLineNums.length-1].toString())+1;
                analType(newStart,newEnd,oldStart,oldEnd,newLines,oldLines,total);

            }
        }
    }

    //准备方法，分析文本的变化类型，存入结果集合中
    public static void analType(int newStart,int newEnd,int oldStart,int oldEnd,Map<Integer,String> newLines,Map<Integer,String> oldLines,int total) {

        //下面开始分析差异区域的变化类型，然后按照类型进行处理
        if((oldEnd-oldStart)>(newEnd-newStart)&&newEnd==newStart) {
            Map oldline=new HashMap();
            for(int i=oldStart;i<oldEnd;i++) {
                //取出被删除的行，存入集合
                oldline.put(i, oldLines.get(i));
            }
            //resultMap静态变量表示最后总的结果，由于本方法会递归执行，total值会随着本方法的递归自增
            //以此来让下次递归时存入的计算结果与本轮的计算结果key值不同，避免前面递归的计算结果被覆盖
            resultMap.put("delete"+total,oldline );
        }

        if((oldEnd-oldStart)==(newEnd-newStart)) {
            Map oldline=new HashMap();
            Map newline=new HashMap();
            for(int i1=oldStart;i1<oldEnd;i1++) {
                oldline.put(i1, oldLines.get(i1));
            }
            for(int i2=newStart;i2<newEnd;i2++) {
                newline.put(i2, newLines.get(i2));
            }

            //收集修改的行，调用下面的方法，进行新旧行匹配
            int number=oldEnd-oldStart;
            Map<Integer, Integer> change=getUpdateLines(oldline,newline,number);
            resultMap.put("update"+total,change );
        }

        if(oldEnd==oldStart&&(oldEnd-oldStart)<(newEnd-newStart)) {
            Map newline=new HashMap();
            for(int i=newStart;i<newEnd;i++) {
                newline.put(i, newLines.get(i));
            }
            resultMap.put("add"+total,newline );
        }

        //说明有新增也有修改
        if(oldEnd!=oldStart&&newEnd!=newStart&&(oldEnd-oldStart)<(newEnd-newStart)) {
            //此时修改的行数是：
            int number=oldEnd-oldStart;
            Map<Integer,String> oldline=new HashMap();
            Map<Integer,String> newline=new HashMap();
            Map<Integer,String> addline=new HashMap();

            for(int i1=oldStart;i1<oldEnd;i1++) {
                oldline.put(i1, oldLines.get(i1));
            }
            for(int i2=newStart;i2<newEnd;i2++) {
                newline.put(i2, newLines.get(i2));
            }
            //获取修改的旧文本行号与新文本行号组成键值对的集合
            Map<Integer, Integer> change=getUpdateLines(oldline,newline,number);
            resultMap.put("update"+total,change );
            //获取新增的行
            for(Integer lineNum1:newline.keySet()) {
                //m是用来检测是否属于修改的行的一个标志，初始值设为0
                int m=0;
                for(Integer lineNum2:change.keySet()) {
                    //说明这是修改的行
                    if(lineNum1==change.get(lineNum2)) {
                        m++;
                    }
                }
                //当内部循环结束，如果m没有自增，说明这不是修好的行，而是增加的行
                if(m==0) {
                    addline.put(lineNum1, newline.get(lineNum1));
                }
            }
            resultMap.put("add"+total,addline);
        }


        //说明有删除也有修改
        if(oldEnd!=oldStart&&newEnd!=newStart&&(oldEnd-oldStart)>(newEnd-newStart)) {
            int number=newEnd-newStart;
            Map<Integer,String> oldline=new HashMap();
            Map<Integer,String> newline=new HashMap();

            Map<Integer,String> addline=new HashMap();

            for(int i1=oldStart;i1<oldEnd;i1++) {
                oldline.put(i1, oldLines.get(i1));
            }
            for(int i2=newStart;i2<newEnd;i2++) {
                newline.put(i2, newLines.get(i2));
            }

            //获取修改的行
            Map<Integer, Integer> change=getUpdateLines(oldline,newline,number);
            resultMap.put("update"+total,change );
            for(Integer lineNum1:oldline.keySet()) {
                //m用来标志是否属于修改的行
                int m=0;
                for(Integer lineNum2:change.keySet()) {
                    //说明这是修改的行
                    if(lineNum1==lineNum2) {
                        m++;
                    }
                }
                //当内部循环结束，如果m没有自增，说明这不是修好的行，而是增加的行
                if(m==0) {
                    addline.put(lineNum1, oldline.get(lineNum1));
                }
            }
            resultMap.put("delete"+total,addline);
        }
    }


    //准备方法，在新旧文本寻找差异区域的起点，oldLines和newLines分别为存储新旧文本行内容的Map集合
    public static Map getBreakPoint(Map<Integer,String> oldLines,Map<Integer,String> newLines) {
        //定义一个集合，用于存储差异区域的起点位置
        Map breakPoint=new HashMap();
        Object[] oldLineNums = oldLines.keySet().toArray();
        //将行号从小到大排序，以便能从上往下遍历每一行
        Arrays.sort(oldLineNums);
        //开始遍历旧文本的每一行
        for  (Object oldLinesNum : oldLineNums) {
            //取出旧文本中的一行以及其行号
            String lineOld=oldLines.get(oldLinesNum);
            //将行号从小到大排序，以便能从上往下遍历每一行
            Object[] newLinesNums =  newLines.keySet().toArray();
            Arrays.sort(newLinesNums);
            //遍历新的文本每一行
            for  (Object newLinesNum : newLinesNums) {
                //取出新文本中的一行以及其行号
                String lineNew=newLines.get(newLinesNum);
                //如果新文本的行内容为空，说明已经对比过，并且对比结果是没有改变，因此直接跳过，
                //取出新文本的下一行来进行比对
                if(lineNew.equals("")) {
                    continue;
                }else {
                    //如果两行内容不一样，说明已经找到差异区域的起始点
                    if(!lineOld.equals(lineNew)) {
                        //存储新旧文本的差异区域的起始行号，然后返回
                        breakPoint.put("oldLinesBreakStart", oldLinesNum);
                        breakPoint.put("newLinesBreakStart", newLinesNum);
                        return breakPoint;
                    }else {
                        //对于已经对比没有改变的行，将行的内容设为空，下次循环比对时直接跳过
                        //以此来实现新旧文本的同步换行，而不是在外层的一轮循环中，内层循环要从头开始
                        newLines.put(Integer.parseInt(newLinesNum.toString()), "");
                        break;
                    }
                }
            }
        }
        //如果对比没有发现不相同的行，说明文本以及没有差异，返回null值即可
        return null;
    }


    //准备方法，寻找差异区域的终点，也就是新旧文本重新复合的点。
    //oldLeftLines和newLeftLines分别表示存储新旧文本从差异区域起点开始剩余行的Map集合
    public static Map getConn(Map<Integer,String> oldLeftLines,Map<Integer,String> newLeftLines,int newLinesStart,Map reConnPoint) {
        //取出旧文本的行号集合，将行号从小到大排序，以便能从上往下遍历每一行
        Object[] oldLinesNums = oldLeftLines.keySet().toArray();
        Arrays.sort(oldLinesNums);
        //取出新文本的行号集合，将行号从小到大排序，以便能从上往下遍历每一行
        Object[] newLinesNums = newLeftLines.keySet().toArray();
        Arrays.sort(newLinesNums);
        int newNumMax=(int) newLinesNums[newLinesNums.length-1];
        for (Object oldLinesNum : oldLinesNums) {
            String lineOld=oldLeftLines.get(oldLinesNum);
            int oldNum=Integer.valueOf(oldLinesNum.toString());
            for(Object newLinesNum : newLinesNums) {
                int newNum=Integer.valueOf(newLinesNum.toString());
                //找到内容相同的行
                if(newLeftLines.get(newNum).equals(oldLeftLines.get(oldNum))) {
                    //已经找到内容相同的行，可以认为差异区域的结束，记录下差异区域终点位置然后返回结果
                    reConnPoint.put("oldLinesConnPoint", oldNum);
                    reConnPoint.put("newLinesConnPoint", newNum);
                    return reConnPoint;
                }
            }
        }
        return reConnPoint;
    }




    //此方法用于读取文件，并且将文件的每一行及其对应的行号存储进Map集合进行返回
    public static Map<Integer,String> readFile(String path) {
        BufferedReader reader = null;
        File file = new File(path);
        if(!file.exists()) {
            System.out.println("文件不存在");
        }
        String tempStr;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            //行号从0开始
            int i=0;
            Map<Integer, String> lines=new HashMap();
            while ((tempStr = reader.readLine()) != null) {
                //读取文本时，每一行采用行号+行文本内容键值对的形式进行存储，行号作为该行的唯一标识
                lines.put(i, tempStr);
                //即将读取下一行的时候，行号自增1
                i++;
            }
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            if(reader!=null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //准备方法，计算两个字符串相同字符的数量
    public static int numJewelsInStones(String J, String S) {
        J=J.trim();
        S=S.trim();
        char[] Ja = J.toCharArray();
        char[] Sa = S.toCharArray();
        int r = 0;
        for (int i = 0;i < Ja.length ; i ++){
            for(int j = 0; j < Sa.length; j++){
                if(Ja[i] == Sa[j])
                    r ++;
            }
        }
        return r;
    }

    //准备方法，将Map集合按照Value值进行排序
    public static List<String> sortMapByValue(Map<String, Integer> map) {
        int size = map.size();
        //通过map.entrySet()将map转换为"1.B.1.e=78"形式的list集合
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(size);
        list.addAll(map.entrySet());
        //通过Collections.sort()排序
        Collections.sort(list, new ValueComparator());
        List<String> keys = new ArrayList<String>(size);
        for (Map.Entry<String, Integer> entry : list){
            // 得到排序后的键值
            keys.add(entry.getKey());
        }
        return keys;
    }

    private static class ValueComparator implements Comparator<Map.Entry<String, Integer>> {
        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
            // compareTo方法 (x < y) ? -1 : ((x == y) ? 0 : 1)
            // 倒序：o2.getValue().compareTo(o1.getValue())，顺序：o1.getValue().compareTo(o2.getValue())
            return o2.getValue().compareTo(o1.getValue());
        }
    }

    //准备方法，找出修改的哪些行。contentOld和contentNew分别表示新旧文本里面在差异区域内的行的集合
    //参数n表示我们需要找的修改前后的行有几对
    public static Map getUpdateLines(Map<Integer,String> contentOld,Map<Integer,String> contentNew,int n) {

        Map<Integer, Integer> resultMap=new HashMap();
        //准备集合，用来储存组队两行的重复字符个数与各自的行号
        Map<String,Integer>samChar=new HashMap();
        for(Integer oldNum:contentOld.keySet()) {
            for(Integer newNum:contentNew.keySet()) {
                //比较两行之间相同字符的数量
                int count=numJewelsInStones(contentOld.get(oldNum),contentNew.get(newNum));
                //将每两行之间的相同字符数量和行号存入集合
                samChar.put(oldNum.toString()+":"+newNum.toString(),count);
            }
        }
        //获取按照value值（也就是重复字符个数）从大到小排序的key值集合，以便取出重复字符最对的组队。
        List<String> keys=sortMapByValue(samChar);

        //取出相同字符数量最多的新旧行对
        for(int i=0;i<n;i++) {
            String lineNumArr=keys.get(i);
            String[] lineNumA=lineNumArr.split(":");
            //重复字符最多的行对视为修改前后的两行
            resultMap.put(Integer.valueOf(lineNumA[0]),Integer.valueOf(lineNumA[1]));

        }
        return resultMap;
    }
}