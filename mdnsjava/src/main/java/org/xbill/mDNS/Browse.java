package org.xbill.mDNS;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.Message;
import org.xbill.DNS.MulticastDNSUtils;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.ResolverListener;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Browse extends MulticastDNSLookupBase
{
    protected static ScheduledExecutorService defaultScheduledExecutor = Executors.scheduledExecutor;
    
    /**
     * The Browse Operation manages individual browse sessions.  Retrying broadcasts. 
     * Refer to the mDNS specification [RFC 6762]
     * 
     * @author Steve Posick
     */
    protected class BrowseOperation implements ResolverListener, Runnable
    {
        private int broadcastDelay = 0;
        
        private ListenerProcessor<ResolverListener> listenerProcessor = new ListenerProcessor<ResolverListener>(ResolverListener.class);
        
        private long lastBroadcast;
        
        
        BrowseOperation()
        {
            this(null);
        }


        BrowseOperation(ResolverListener listener)
        {
            if (listener != null)
            {
                registerListener(listener);
            }
        }


        Message[] getQueries()
        {
            return queries;
        }
        
        
        boolean answersQuery(Record record)
        {
            if (record != null)
            {
                for (Message query : queries)
                {
                    for (Record question : MulticastDNSUtils.extractRecords(query, Section.QUESTION))
                    {
                        Name questionName = question.getName();
                        Name recordName = record.getName();
                        int questionType = question.getType();
                        int recordType = record.getType();
                        int questionDClass = question.getDClass();
                        int recordDClass = record.getDClass();
                        
                        if ((questionType == Type.ANY || questionType == recordType) &&
                            (questionName.equals(recordName) || questionName.subdomain(recordName) ||
                            recordName.toString().endsWith("." + questionName.toString())) &&
                            (questionDClass == DClass.ANY || (questionDClass & 0x7FFF) == (recordDClass & 0x7FFF)))
                        {
                            return true;
                        }
                    }
                }
            }
            
            return false;
        }
        
        
        boolean matchesBrowse(Message message)
        {
            Record[] thatAnswers = MulticastDNSUtils.extractRecords(message, Section.ANSWER, Section.AUTHORITY, Section.ADDITIONAL);
            
            for (Record thatAnswer : thatAnswers)
            {
                if (answersQuery(thatAnswer))
                {
                    return true;
                }
            }
            
            return false;
        }
        
        
        ResolverListener registerListener(ResolverListener listener)
        {
            return listenerProcessor.registerListener(listener);
        }
        
        
        ResolverListener unregisterListener(ResolverListener listener)
        {
            return listenerProcessor.unregisterListener(listener);
        }
        

        public void receiveMessage(Object id, Message message)
        {
            if (message != null)
            {
                Header header = message.getHeader();
                
                if (header.getFlag(Flags.QR) || header.getFlag(Flags.AA))
                {
                    if (matchesBrowse(message))
                    {
                        listenerProcessor.getDispatcher().receiveMessage(id, message);
                    }
                }
            }
        }


        public void handleException(Object id, Exception e)
        {
            listenerProcessor.getDispatcher().handleException(id, e);
        }
        
        
        public void run()
        {
            if (mdnsVerbose)
            {
                long now = System.currentTimeMillis();
                System.out.println("Broadcasting Query for Browse." + (lastBroadcast <= 0 ? "" : " Last broadcast was " + ((double) ((double) (now - lastBroadcast) / (double) 1000)) + " seconds ago.") );
                lastBroadcast = System.currentTimeMillis();
            }
            
            try
            {
                broadcastDelay = broadcastDelay > 0 ? Math.min(broadcastDelay * 2, 3600) : 1;
                scheduledExecutor.schedule(this, broadcastDelay, TimeUnit.SECONDS);
                
                if (mdnsVerbose)
                {
                    System.out.println("Broadcasting Query for Browse Operation.");
                }
                
                for (Message query : queries)
                {
                    querier.broadcast((Message) query.clone(), false);
                }
            } catch (Exception e)
            {
                System.err.println("Error broadcasting query for browse - " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }


        public void close()
        {
            try
            {
                listenerProcessor.close();
            } catch (IOException e)
            {
                // ignore
            }
        }
    }
    
    protected List browseOperations = new LinkedList();
    
    protected ScheduledExecutorService scheduledExecutor = defaultScheduledExecutor;
   

    protected Browse()
    throws IOException
    {
        super();
    }


    public Browse(Name... names)
    throws IOException
    {
        super(names);
    }
    
    
    public Browse(Name[] names, int type)
    throws IOException
    {
        super(names, type);
    }
    
    
    public Browse(Name[] names, int type, int dclass)
    throws IOException
    {
        super(names, type, dclass);
    }
    
    
    protected Browse(Message message)
    throws IOException
    {
        super(message);
    }
    
    
    public Browse(String... names)
    throws IOException
    {
        super(names);
    }
    
    
    public Browse(String[] names, int type)
    throws IOException
    {
        super(names, type);
    }
    
    
    public Browse(String[] names, int type, int dclass)
    throws IOException
    {
        super(names, type, dclass);
    }
    
    
    public static void setDefaultScheduledExecutor(ScheduledExecutorService scheduledExecutor)
    {
        if (scheduledExecutor != null)
        {
            defaultScheduledExecutor = scheduledExecutor;
        }
    }
    
    
    public void setScheduledExecutor(ScheduledExecutorService scheduledExecutor)
    {
        if (scheduledExecutor != null)
        {
            this.scheduledExecutor = scheduledExecutor;
        } else
        {
            this.scheduledExecutor = defaultScheduledExecutor;
        }
    }


    /**
     * @param listener
     * @throws IOException
     */
    public synchronized void start(ResolverListener listener)
    {
        if (listener == null)
        {
            if (mdnsVerbose)
            {
                System.err.println("Error sending asynchronous query, listener is null!");
            }
            throw new NullPointerException("Error sending asynchronous query, listener is null!");
        }
        
        if (queries == null || queries.length == 0)
        {
            if (mdnsVerbose)
            {
                System.err.println("Error sending asynchronous query, No queries specified!");
            }
            throw new NullPointerException("Error sending asynchronous query, No queries specified!");
        }
        
        BrowseOperation browseOperation = new BrowseOperation(listener);
        browseOperations.add(browseOperation);
        querier.registerListener(browseOperation);
        
        scheduledExecutor.submit(browseOperation);
    }


    public void close()
    throws IOException
    {
        for (Object o : browseOperations)
        {
            BrowseOperation browseOperation = (BrowseOperation) o;
            try
            {
                browseOperation.close();
            } catch (Exception e)
            {
                // ignore
            }
        }
        
        if (scheduledExecutor != null && scheduledExecutor != defaultScheduledExecutor)
        {
            scheduledExecutor.shutdownNow();
        }
    }
}