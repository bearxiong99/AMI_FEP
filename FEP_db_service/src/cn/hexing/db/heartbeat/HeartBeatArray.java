package cn.hexing.db.heartbeat;

public class HeartBeatArray{
	private int columnIndex;
	private HeartBeat[] heartBeats;
	private int index = 0;
	
	public HeartBeatArray(int columnIndex,int batchSize){
		super();
		this.columnIndex = columnIndex;
		heartBeats = new HeartBeat[batchSize];
	}

	public void addHeartBeat(HeartBeat heartBeat)
			throws ArrayIndexOutOfBoundsException{
		heartBeats[index++] = heartBeat;
	}
	
	public HeartBeat getHeartBeat(int i)
		throws ArrayIndexOutOfBoundsException{
		return heartBeats[i];
	}
	
	public int getSize(){
		return index;
	}
	
	public boolean isFull(){
		return index >= heartBeats.length;
	}
	
	public void initArray(){
		index = 0;
	}
	
	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}
}
