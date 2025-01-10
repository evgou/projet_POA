package misc;

import jade.lang.acl.ACLMessage;

public class FishMarketPerformatif {
    public static final int TO_ANNOUNCE = ACLMessage.CFP;               // = 3
    public static final int TO_ATTRIBUTE = ACLMessage.ACCEPT_PROPOSAL;  // = 0
    public static final int TO_GIVE = ACLMessage.AGREE;                 // = 1
    public static final int REP_BID_OK = ACLMessage.INFORM;             // = 7
    public static final int REP_BID_NOK = ACLMessage.REFUSE;            // = 14
    public static final int TO_BID = ACLMessage.PROPOSE;                // = 11
    public static final int TO_PAY = ACLMessage.CONFIRM;                // = 4
}
