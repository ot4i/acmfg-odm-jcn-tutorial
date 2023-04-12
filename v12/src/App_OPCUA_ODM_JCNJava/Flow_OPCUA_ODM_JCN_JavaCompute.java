import java.util.HashMap;
import java.util.Map;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbODMRuleset;
import com.ibm.broker.plugin.MbODMRulesetExecutionResponse;
import com.ibm.broker.plugin.MbODMServer;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

import acmfgODMExample.SimpleACObject;

public class Flow_OPCUA_ODM_JCN_JavaCompute extends MbJavaComputeNode {

	/**
		 * onSetup() is called during the start of the message flow allowing
		 * configuration to be read/cached, and endpoints to be registered.
		 *
		 * Calling getPolicy() within this method to retrieve a policy links this
		 * node to the policy. If the policy is subsequently redeployed the message
		 * flow will be torn down and reinitialized to it's state prior to the policy
		 * redeploy.
		 *
		 * @throws MbException
		 */
	@Override
	public void onSetup() throws MbException {

		// Setup the reference to the ODMServer policy and policy project.
		// This should be of the form '{<PolicyProjectName>}:<PolicyName>'
		String odmServerPolicyName = "{Policy_OPCUA_ODM_Project}:Policy_OPCUA_ODM_Policy";

		// Retrieve the ODM server connection.
		// This needs to be resolved in the onSetup method.
		odmServer = getODMServer(odmServerPolicyName);

		// Retrieve the ODM Rule from the server.
		// This can be completed in the evaluate method, but looking up the 
		// ODM Rule in the onSetup method speeds up first message execution.
		odmRuleset = odmServer.getRuleset("/ODM_JCN_Deployment/Latest/JCN_Operation/Latest");

	}

	private MbODMServer odmServer = null;
	private MbODMRuleset odmRuleset = null;

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		try {
			// create new message as a copy of the input
			//MbMessage outMessage = new MbMessage(inMessage);
			
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage();
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// ----------------------------------------------------------
			// Start of user code

			Map<String, Object> ruleInParameters = new HashMap<String, Object>();

			// Build up the map of parameters for rule execution
			// For example:
			//   MyObject myObj1 = new MyObject();
			//   MbElement propertyElement = inMessage.getRootElement().getLastChild().getFirstElementByPath("Property");
			//   myObj1.setAProperty(propertyElement.getValueAsString());
			//   ruleInParameters.put("ruleParameter1", myObj1);
			//   RuleSet Parameters are as follows:
			//   Name         Type      Direction       Kind
			//   ACObject   acmfgODMExample.SimpleACObject    INOUT   Java
			
			float humidity=0;
			float temperature=0;
			 
			MbElement propertyElement = inMessage.getRootElement().getFirstElementByPath("XMLNSC/Items/Item");
		
			while (propertyElement != null)
			{				 			 					 
				MbElement me_name = propertyElement.getFirstElementByPath("name");
					 
				if(me_name!=null)
				{
					MbElement me1 = propertyElement.getFirstElementByPath("Value");
					 
					if(me1!=null) 
					{
						MbElement mData = me1.getFirstElementByPath("Data");
							 
						if(mData != null && mData.getValueAsString() != null)
						{								 
								if(me_name.getValueAsString().equalsIgnoreCase("AC1_Humidity"))
							 		humidity = Float.parseFloat(mData.getValueAsString());
							 	
							 	if(me_name.getValueAsString().equalsIgnoreCase("AC1_temperature"))
							 		temperature = Float.parseFloat(mData.getValueAsString());
						 }
					}
				}					 			 
				propertyElement = propertyElement.getNextSibling();				 
			}
			

			//Map<String, Object> ruleInParameters = new HashMap<String, Object>();

			// Build up the map of parameters for rule execution
			// For example:
			SimpleACObject myObj1 = new SimpleACObject();			   
			myObj1.setHumidity(humidity);
			myObj1.setTemperature(temperature);
			myObj1.setStatus("OK");
				   
			//   RuleSet Parameters are as follows:
			//   Name         Type      Direction       Kind
			//   varSetJ   acmfgODMExample.SimpleACObject    INOUT   Java		
			ruleInParameters.put("ACObject", myObj1);			
			
			// Execute the rule passing in the map of parameters
			MbODMRulesetExecutionResponse ruleResponse = odmRuleset.execute(ruleInParameters);
			Map<String, Object> ruleOutParameters = ruleResponse.getOutputParameters();

			// Evaluate the ruleResponse and map the out parameters into the output message
			// For example:
			//   MyResultObject myResult1 = (MyResultObject)ruleOutParameters.get("ruleResponseParameter1");
			//   MbElement outPropertyElement = inMessage.getRootElement().getLastChild().createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "OutProperty", myResult1.getAProperty());

			SimpleACObject myResult1 = (SimpleACObject)ruleOutParameters.get("ACObject");			  
			        
			MbElement outRoot = outMessage.getRootElement().createElementAsLastChild("XMLNSC");
			MbElement m_ACObject = outRoot.createElementAsLastChild(MbElement.TYPE_NAME,"ACObjectStatus",null);
			  
		    m_ACObject.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Status", myResult1.getStatus());
		    m_ACObject.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Temperature", Float.toString(myResult1.getTemperature()));
		    m_ACObject.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Humidity", Float.toString(myResult1.getHumidity()));
	
			// End of user code
			// ----------------------------------------------------------
		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(), null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);

	}

	/**
	 * onPreSetupValidation() is called during the construction of the node
	 * to allow the node configuration to be validated.  Updating the node
	 * configuration or connecting to external resources should be avoided.
	 *
	 * @throws MbException
	 */
	@Override
	public void onPreSetupValidation() throws MbException {
	}

	/**
	 * onStart() is called as the message flow is started. The thread pool for
	 * the message flow is running when this method is invoked.
	 *
	 * @throws MbException
	 */
	@Override
	public void onStart() throws MbException {
	}

	/**
	 * onStop() is called as the message flow is stopped. 
	 *
	 * The onStop method is called twice as a message flow is stopped. Initially
	 * with a 'wait' value of false and subsequently with a 'wait' value of true.
	 * Blocking operations should be avoided during the initial call. All thread
	 * pools and external connections should be stopped by the completion of the
	 * second call.
	 *
	 * @throws MbException
	 */
	@Override
	public void onStop(boolean wait) throws MbException {
	}

	/**
	 * onTearDown() is called to allow any cached data to be released and any
	 * endpoints to be deregistered.
	 *
	 * @throws MbException
	 */
	@Override
	public void onTearDown() throws MbException {
	}

}
