import java.text.ParseException;
import java.util.*;

public class Main {



    public static class KWSAC {


        private int K;
        private int C;
        private int B;
        private HashMap<Integer, Set> items;
        private int dHit=0;
        private int iHit=0;
        private int dMiss=0;
        private int iMiss=0;
        private int dRequests=0;
        private int iRequests=0;
        private float dHitRate=0;
        private float dMissRate=0;
        private float iHitRate=0;
        private float iMissRate=0;
        private int demandFetch=0;
        private int copiesBack=0;
        private String type;
        private int dReplaces=0;
        private int iReplaces=0;
        private String writePolicy;
        private String allocatePolicy;


        public KWSAC(String type,String writePolicy,String allocatePolicy,int K, int C , int B) {

            this.type=type;
            this.writePolicy=writePolicy;
            this.allocatePolicy=allocatePolicy;
            this.K = K;
            this.C = C;
            this.B=B;
            items=new HashMap<>();
            construct();
        }

        public void construct(){


            int numberOfSets=C/(B*K);

            for (int i=0;i<numberOfSets;i++){

                Set set=new Set(K,this);

                items.put(i,set);

            }

        }

        public void setCopiesBack(int copiesBack) {

            this.copiesBack = copiesBack;
        }

        public int getCopiesBack() {
            return copiesBack;
        }


        public int getIReplaces() {
            return iReplaces;
        }


        public void setIReplaces(int iReplaces) {
            this.iReplaces = iReplaces;
        }


        public void readInstruction(Address givenAddress){

            iRequests++;
            int offset=givenAddress.getOffset();
            int setIndex=givenAddress.getSetIndex();
            int tag=givenAddress.getTag();


            if( items.get(setIndex).findInCache(tag,offset,givenAddress)==1){

                iHit++;
                items.get(setIndex).updateSetOrder(tag);


            }else {

                iMiss++;
                Block b=new Block(tag,B,items.get(setIndex));
                b.addAddressToBlock(givenAddress,offset);
                items.get(setIndex).addBlockToSet(b,"instruction","null");
                demandFetch+=(B/4);

            }
        }


        public int getDReplaces() {
            return dReplaces;
        }

        public void setDReplaces(int replaces) {
            this.dReplaces = replaces;
        }

        public void dataFunction(Address givenAddress, String operation){

            dRequests++;
            int offset=givenAddress.getOffset();
            int setIndex=givenAddress.getSetIndex();
            int tag=givenAddress.getTag();

            if(items.get(setIndex).findInCache(tag,offset,givenAddress)==1){

                dHit++;
                if (operation.equals("wb")) {

                    items.get(setIndex).getBlockOfSet(tag).setDirty(1);

                }
                if(operation.equals("wt")){

                    copiesBack++;
                }
                items.get(setIndex).updateSetOrder(tag);

            }else {

                dMiss++;

                if (allocatePolicy.equals("wa")|| operation.equals("read")) {

                    Block b = new Block(tag, B, items.get(setIndex));

                    if (operation.equals("wb")){
                        b.setDirty(1);
                    }

                    b.addAddressToBlock(givenAddress, offset);
                    items.get(setIndex).addBlockToSet(b,"data",operation);
                    demandFetch+=(B/4);

                    if ( operation.equals("wt")&& allocatePolicy.equals("wa")) {

                        copiesBack++;
                    }

                }



                if(allocatePolicy.equals("nw") && !operation.equals("read")){

                    copiesBack++;
                }

            }
        }


        public void finalWork(){


            for (Map.Entry<Integer, Set> entry:items.entrySet()) {

                int key=entry.getKey();
                Set value=entry.getValue();

                for (Block b:value.getSetOfBlocks()) {

                    if(b.getDirty()==1){
                        copiesBack+=(B/4);
                    }
                }

            }

        }


        public int getDemandFetch() {
            return demandFetch;
        }

        public void printCacheStatistics(int dFlag, int iFlag,int fetch) throws ParseException {

            computeRates();

            if(iFlag==1) {
                System.out.println("***CACHE STATISTICS***");
                System.out.println("INSTRUCTIONS");
                System.out.println("accesses: " + iRequests);
                System.out.println("misses: " + iMiss);
                System.out.printf("miss rate:  %.4f",iMissRate );
                System.out.printf(" (hit rate  %.4f" , iHitRate );
                System.out.printf(")\n");
                System.out.println("replace: " + iReplaces);
            }
            if (dFlag==1) {
                System.out.println("DATA");
                System.out.println("accesses: " + dRequests);
                System.out.println("misses: " + dMiss);
                System.out.printf("miss rate:  %.4f",dMissRate);
                System.out.printf(" (hit rate  %.4f" , dHitRate );
                System.out.printf(")\n");
                System.out.println("replace: " + dReplaces);
                System.out.println("TRAFFIC (in words)");
                if (type.equals("Split I- D-cache")) {
                    int total=demandFetch+fetch;
                    System.out.println("demand fetch: " + total);
                }else{
                    System.out.println("demand fetch: " + demandFetch);
                }
                System.out.println("copies back: " + copiesBack);
            }
        }



        public void printCacheSettings(int size2){

            finalWork();
            if (type.equals("Unified I- D-cache")) {
                System.out.println("***CACHE SETTINGS***");
                System.out.println(type);
                System.out.println("Size: " + C);
                System.out.println("Associativity: " + K);
                System.out.println("Block size: " + B);
                switch (writePolicy) {

                    case "wb":
                        System.out.println("Write policy: WRITE BACK");
                        break;
                    case "wt":
                        System.out.println("Write policy: WRITE THROUGH");
                        break;

                }
                switch (allocatePolicy) {

                    case "wa":
                        System.out.println("Allocation policy: WRITE ALLOCATE");
                        break;
                    case "nw":
                        System.out.println("Allocation policy: WRITE NO ALLOCATE");
                        break;

                }
                System.out.println();

            }else if(type.equals("Split I- D-cache")){

                System.out.println("***CACHE SETTINGS***");
                System.out.println(type);
                System.out.println("I-cache size: "+size2);
                System.out.println("D-cache size: " + C);
                System.out.println("Associativity: " + K);
                System.out.println("Block size: " + B);
                switch (writePolicy) {

                    case "wb":
                        System.out.println("Write policy: WRITE BACK");
                        break;
                    case "wt":
                        System.out.println("Write policy: WRITE THROUGH");
                        break;

                }
                switch (allocatePolicy) {

                    case "wa":
                        System.out.println("Allocation policy: WRITE ALLOCATE");
                        break;
                    case "nw":
                        System.out.println("Allocation policy: WRITE NO ALLOCATE");
                        break;

                }
                System.out.println();

            }

        }


        public void process(Address address, int operation){

            switch (operation){

                case 0 :
                    dataFunction(address,"read");
                    break;

                case 1:

                    dataFunction(address,writePolicy);
                    break;

                case 2:
                    readInstruction(address);
                    break;

            }



        }

        public int getB() {
            return B;
        }

        public void computeRates(){

            if (dRequests!=0) {
                dHitRate =  Math.round(( dHit / (float) dRequests)*10000D)/(float)10000D;
                dMissRate = Math.round(( dMiss / (float) dRequests)*10000D)/(float)10000D;
            }
            if (iRequests!=0) {
                iHitRate =  Math.round((iHit / (float) iRequests)*10000D)/(float)10000D;
                iMissRate = Math.round(( iMiss / (float) iRequests)*10000D)/(float)10000D;
            }

        }

    }


    public static class Set {


        private LinkedList<Block> setOfBlocks;
        private int K;
        private KWSAC cache;

        public Set( int K ,KWSAC cache) {

            this.cache=cache;
            this.K = K;
            setOfBlocks=new LinkedList<>();
        }


        public void addBlockToSet(Block block , String request, String operation) {

            if (setOfBlocks.size() < K) {

                setOfBlocks.add(block);


            } else {


                if ( setOfBlocks.getFirst().getDirty() == 1) {

                    cache.setCopiesBack(cache.getCopiesBack()+(cache.getB()/4));

                }

                setOfBlocks.removeFirst();
                setOfBlocks.add(block);

                if (request.equals("data")) {

                    cache.setDReplaces(cache.getDReplaces() + 1);
                }
                else if(request.equals("instruction")){

                    cache.setIReplaces(cache.getIReplaces() + 1);

                }
            }
        }

        public void updateSetOrder(int tag){

            Block block = null;

            for (Block b : setOfBlocks) {

                if (tag == b.getTag()) {

                    block=b;
                    break;

                }

            }

            setOfBlocks.remove(block);
            setOfBlocks.add(block);

        }


        public int findInCache(int tag , int offset , Address address){

            for (Block b:setOfBlocks) {

                if(b.getTag()==tag){

                    if (b.findAddress(offset,address)==0){

                        b.addAddressToBlock(address,offset);
                    }
                    return 1;

                }

            }

            return 0;

        }

        public Block getBlockOfSet(int tag) {

            for (Block b : setOfBlocks) {

                if (b.getTag()==tag) {

                    return b;
                }


            }
            return null;
        }


        public LinkedList<Block> getSetOfBlocks() {
            return setOfBlocks;
        }
    }


    public static class Block {


        private HashMap<Integer, Address> content =new HashMap<>(); //offset address
        private int tag;
        private int blockSize;
        private Set blockSet;
        private int dirty=0;

        public Block(int tag, int blockSize, Set blockSet) {

            this.tag=tag;
            this.blockSize = blockSize;
            this.blockSet = blockSet;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Block block = (Block) o;
            return blockSize == block.blockSize &&
                    Objects.equals(tag, block.tag) &&
                    blockSet.equals(block.blockSet);
        }

        public int getDirty() {
            return dirty;
        }

        public void setDirty(int dirty) {
            this.dirty = dirty;
        }

        public int getTag() {
            return tag;
        }

        public int findAddress(int offset , Address address){

            if(content.get(offset)!=null && content.get(offset).equals(address)){

                return 1;
            }else
                return 0;

        }

        public void addAddressToBlock(Address address, int offset){

            address.setBlock(this);
            content.put(offset,address);

        }


    }

    public static class Address {


        private String givenAddress;
        private Block block;
        private int offset;
        private int setIndex;
        private int tag;


        public Address(String givenAddress , int C , int B ,int K) {
            this.givenAddress = givenAddress;
            computeSections(C,B,K);
        }


        public void computeSections(int C, int B ,int K){

            int num=Integer.parseInt(baseConversion(givenAddress,16,10));

            if(num!=0) {

                offset=num%B;
                setIndex = (num / B) % (C / (B * K));
                tag = (num / B) / (C / (B * K));

            }else{

                offset=0;
                setIndex=0;
                tag=0;

            }
        }


        public String getGivenAddress() {
            return givenAddress;
        }


        public Block getBlock() {
            return block;
        }

        public int getOffset() {
            return offset;
        }

        public int getSetIndex() {
            return setIndex;
        }

        public int getTag() {
            return tag;
        }

        public void setBlock(Block block) {
            this.block = block;
        }



        @Override
        public String toString() {
            return "Address{" +
                    "givenAddress='" + givenAddress + '\'' +
                    '}';
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Address address = (Address) o;
            return Objects.equals(givenAddress, address.givenAddress);
        }



        public String baseConversion(String number, int sBase, int dBase)
        {
            return Integer.toString(
                    Integer.parseInt(number, sBase),
                    dBase);
        }

    }


    public static void main(String[] args) throws ParseException {


        Scanner scanner = new Scanner(System.in);
        String line=scanner.nextLine();
        String[] items=line.split(" - ");
        int B = Integer.parseInt(items[0]);
        int type = Integer.parseInt(items[1]);
        int K =Integer.parseInt(items[2]) ;
        String writePolicy = items[3];
        String allocatePolicy = items[4];


        if (type == 0) {

            int C = scanner.nextInt();
            KWSAC cache=new KWSAC("Unified I- D-cache",writePolicy,allocatePolicy,K,C,B);
            String ignore=scanner.nextLine();
            line=scanner.nextLine();

            while (!line.isEmpty()) {

                String[] parts=line.split("\\s+");
                int operation = Integer.parseInt(parts[0]);
                String gAddress = parts[1];
                Address address=new Address(gAddress,C,B,K);
                cache.process(address,operation);
                line=scanner.nextLine();

            }
            cache.printCacheSettings(0);
            cache.printCacheStatistics(1,1,0);

            return;

        }else if(type==1){

            line=scanner.nextLine();
            String[] parts=line.split(" - ");
            int C1= Integer.parseInt(parts[0]);
            int C2= Integer.parseInt(parts[1]);

            KWSAC iCache=new KWSAC("Split I- D-cache",writePolicy,allocatePolicy,K,C1,B);
            KWSAC dCache=new KWSAC("Split I- D-cache",writePolicy,allocatePolicy,K,C2,B);


            line=scanner.nextLine();

            while (!line.isEmpty()) {

                String[] part=line.split("\\s+");
                int operation = Integer.parseInt(part[0]);
                String gAddress = part[1];

                if (operation==2){

                    Address address=new Address(gAddress,C1,B,K);
                    iCache.process(address,operation);
                    line=scanner.nextLine();

                }else {

                    Address address=new Address(gAddress,C2,B,K);
                    dCache.process(address,operation);
                    line=scanner.nextLine();


                }


            }

            dCache.printCacheSettings(C1);
            iCache.printCacheStatistics(0,1,0);
            dCache.printCacheStatistics(1,0,iCache.getDemandFetch());
            return;

        }


    }


}
