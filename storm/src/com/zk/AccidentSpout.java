package com.zk;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import com.google.gson.Gson;

public class AccidentSpout implements IRichSpout {
	
	private static final long serialVersionUID = 1L;
	
	//Atributo que guarda a referencia do OutputSpout para usar no metodo nextTuple()
	private SpoutOutputCollector collector;

	/*
	 * Aqui estamos implementando um m�todo do IRichSpout. 
	 * Esse m�todo � chamado pela plataforma/storm no in�cio da topologia.
	 */
	@Override
	public void open(Map arg0, TopologyContext context, SpoutOutputCollector arg2) {
		this.collector = arg2;
	}

	/*
	 * Aqui estamos implementando um m�todo do IRichSpout. 
	 * Esse m�todo � chamado continuamente pela plataforma. 
	 */
	@Override
	public void nextTuple() {
		System.out.println("---------------------");
		System.out.println("AccidentSpout --> nextTuple");
		
		try(Reader reader = new InputStreamReader(Main.class.getResourceAsStream("data.json"), "UTF-8")){
			Gson gson = new Gson();
			AccidentModel model = gson.fromJson(reader, AccidentModel.class);
			for (AccidentModel.Container container : model) {
			    String innerJson = gson.toJson(container);
			   // System.out.println(container.situacao);
			    this.collector.emit(new Values(innerJson));
			    Thread.sleep(1000);
			}
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	/*
	 * Aqui estamos implementando um m�todo do IRichSpout. 
	 * Esse m�todo declara os nomes dos campos que o Spout vai enviar para os bolts 
	 */
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("linha"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void ack(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fail(Object arg0) {
		// TODO Auto-generated method stub

	}

}
