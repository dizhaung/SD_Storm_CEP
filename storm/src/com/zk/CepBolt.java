package com.zk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.esotericsoftware.minlog.Log;
import com.espertech.esper.client.*;
import com.espertech.esper.event.bean.BeanEventBean;
import com.espertech.esper.event.map.MapEventBean;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CepBolt implements IRichBolt {

    private static final Logger LOG = LoggerFactory.getLogger(CepBolt.class);

    private static final long serialVersionUID = 1L;
    private EPRuntime cep;


    /*
	 * M�todo chamado pela plataforma. Como este bolt est� localizado no final da nossa topologia, 
	 * n�o � preciso guardar refer�ncia para os objetos passados como par�metro.
	 */
	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {

        // Cria uma configuração
        Configuration cepConfig = new Configuration();

        // Registra os objetos que o engenho CEP vai tratar
        cepConfig.addEventType("accident", AccidentModel.Container.class.getName());
        cepConfig.addEventType("infringement", InfringementModel.Container.class.getName());

        // Configura o engine com seu config
        EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);

        // Registra a consulta via CEP Administrator
        EPAdministrator cepAdm = cep.getEPAdministrator();
        EPStatement cepStatementAccident = cepAdm.createEPL("select * from accident");
        //EPStatement cepStatementInfringement = cepAdm.createEPL("select * from infringement");


        //EPStatement cepStatementInfringement = cepAdm.createPattern("every (infra=infringement(infracao='6050'))");

        //String p = "every i=infringement -> a=accident(endereco = i.localcometimento )";
/*

        String p = "select * from pattern [beginevent=infringement(localcometimento = 'AV GOV AGAMENON MAGALHAES')" +
        "    -> middleevent=infringement(localcometimento != beginevent.localcometimento)" +
                "         until endevent=accident(endereco = beginevent.localcometimento)]";
*/

         String p = "every(inicio=infringement)" +
                "    -> infringement(localcometimento != inicio.localcometimento)" +
                "         until fim=accident(endereco = inicio.localcometimento)";


        //PEGA AS INFRACOES E OS ACIDENTES DE QUALQUER LOCAL
        /*String p = "every(infracao=infringement(localcometimento = 'AV GOV AGAMENON MAGALHAES'))" +
                "->accident(endereco = infracao.localcometimento)";
*/

//String p = "every(a=accident)-> infringement (localcometimento = a.endereco)";

//        String p = "every(a=accident)-> every(infringement (localcometimento = a.endereco))";

       // String p = "select * from infringement";

        EPStatement cepStatementInfringement = cepAdm.createPattern(p);//where timer:within(30 sec)

        //Adiciona o listener para receber eventos do engenho
        /*cepStatementAccident.addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {

                try {
                    if(newEvents == null) return;

                    for (EventBean event: newEvents) {
                        Object ob = event.getUnderlying();
                       // System.out.println("PASSOUUUUUUUUUUUUUUUUUUUUUU");
                      // System.err.println(new Gson().toJson(ob, InfringementModel.Container.class));
                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
*/
        cepStatementAccident.addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {

                // System.out.println("PASSOUUUUUUUUUUUUUUUUUUUUUU");

                try {
                    if(newEvents == null) return;

                    for (EventBean event: newEvents) {
                        Object o = event.getUnderlying();

                        if(o instanceof AccidentModel.Container) {
                            //Log.log(Metrics.verboseCEP, "New taxi...");

                            //InfringementModel.Container p = (InfringementModel.Container)o;
                            //System.out.println(p.getInfracao());
      //                      System.out.println(((AccidentModel.Container)o).getTipo());

                        } else if(o instanceof Map) {
                            MapEventBean map = (MapEventBean) event;
                            Map<String, Object> events = map.getProperties();

                            Set<String> keys = events.keySet();
                            Iterator<String> iKeys = keys.iterator();

                            while(iKeys.hasNext()) {
                                String key = iKeys.next();

//                                System.out.println("TEST_____ " + events.get(key).getClass().getName());


                                Object aux = null;

                                if(events.get(key) instanceof BeanEventBean){
                                    aux= ((BeanEventBean) events.get(key)).getUnderlying();
                                }else if(events.get(key) instanceof EventBean){
                                    aux= ((EventBean) events.get(key)).getUnderlying();
                                }

                                if(aux instanceof InfringementModel.Container){
  //                                  System.out.println("Infracao :>>" + ((InfringementModel.Container)(aux)).getLocalcometimento());
                                }else if(aux instanceof AccidentModel.Container){
      //                             System.out.println("Acidente :>>" + ((AccidentModel.Container)(aux)).getEndereco());
                                }


                            }

                            //System.out.println(events.get("count(*)"));
                            //System.out.println(events.get("QTD"));

                        } else {
                            System.out.println(o.toString());
                        }


                        //Gson g = new Gson();
                        //System.err.println(g.toJson((InfringementModel.Container)ob, InfringementModel.Container.class));
                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        cepStatementInfringement.addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {

                // System.out.println("PASSOUUUUUUUUUUUUUUUUUUUUUU");

                try {
                    if(newEvents == null) return;

                    for (EventBean event: newEvents) {
                        Object o = event.getUnderlying();

                        if(o instanceof InfringementModel.Container) {
                            //Log.log(Metrics.verboseCEP, "New taxi...");

                            //InfringementModel.Container p = (InfringementModel.Container)o;
                            //System.out.println(p.getInfracao());
//                            System.out.println(((InfringementModel.Container)o).getInfracao());
                            LOG.info(((InfringementModel.Container)o).getInfracao());

                        } else if(o instanceof Map) {
                            MapEventBean map = (MapEventBean) event;
                            Map<String, Object> events = map.getProperties();

                            Set<String> keys = events.keySet();
                            Iterator<String> iKeys = keys.iterator();

                            while(iKeys.hasNext()) {
                                String key = iKeys.next();

//                                System.out.println("TEST_____ " + events.get(key).getClass().getName());

                                Object aux = null;

                                if(events.get(key) instanceof BeanEventBean){
                                    aux= ((BeanEventBean) events.get(key)).getUnderlying();
                                }else if(events.get(key) instanceof EventBean){
                                    aux= ((EventBean) events.get(key)).getUnderlying();
                                }

                                if(aux instanceof InfringementModel.Container){
  //                                  System.out.println("Infracao :>>" + ((InfringementModel.Container)(aux)).getLocalcometimento());
                                    LOG.info("Infracao :>>" + ((InfringementModel.Container)(aux)).getLocalcometimento());
                                }else if(aux instanceof AccidentModel.Container){
                                    //System.out.println("Acidente :>>" + ((AccidentModel.Container)(aux)).getEndereco());
                                    LOG.info("Acidente :>>" + ((AccidentModel.Container)(aux)).getEndereco());
                                }


                            }

                            //System.out.println(events.get("count(*)"));
                            //System.out.println(events.get("QTD"));

                        } else {
      //                      System.out.println(o.toString());
                            LOG.info(o.toString());
                        }


                        //Gson g = new Gson();
                        //System.err.println(g.toJson((InfringementModel.Container)ob, InfringementModel.Container.class));
                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                    LOG.error(ex.getMessage());
                }
            }
        });

        // Pega a instancia do engenho/core para associar ao Stream e injetar os dados
        this.cep = cep.getEPRuntime();

	}

    /*
	 * M�todo chamado pela plataforma/storm. A cada nova tupla (registro do stream) esse m�todo � chamado.
	 * Aqui temos um simples c�digo utilizado para contar e imprimir os dados sumarizados.
	 */
	@Override
	public void execute(Tuple input) {
		//System.out.println("CepBolt --> execute");

		Object word = input.getValue(0);

        //System.out.println("CHEGOOUUUU ===>>> " + word);
		
		//Gson gson = new Gson();
		
		//System.out.println("IDDD ==>> " + input.getSourceTask());
		
		// processamento

        this.cep.sendEvent(word);
        /*
		if (word instanceof AccidentModel.Container) {
			//AccidentModel.Container model = gson.fromJson(word, AccidentModel.Container.class);
			//System.out.println("<<< ACIDENTE >>>" + ((AccidentModel.Container) word).getEndereco());
            this.cep.sendEvent(word);
		}
		else if (word instanceof InfringementModel.Container){
	//	    System.out.println("<<< INFRACAO >>>" + ((InfringementModel.Container) word).getLocalcometimento());
            this.cep.sendEvent(word);
        }*/

	}

	/*
	 * M�todo chamado pela plataforma. Aqui devemos declarar os nomes dos campos que esse bolt emite.
	 * Como esse bolt est� no fim da topologia, ele nao emite nada (logo, nao � preciso declarar nenhum campo). 
	 */
	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}
