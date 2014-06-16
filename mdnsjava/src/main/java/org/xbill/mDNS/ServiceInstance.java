package org.xbill.mDNS;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xbill.DNS.Name;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ServiceInstance implements Serializable
{
    private static final long serialVersionUID = 201210181454L;
    
    private final List pointers = new ArrayList();
    
    private final ServiceName name;
    
    private Name host;
    
    private List addresses = new ArrayList();
    
    private int priority;
    
    private int weight;
    
    private int port;
    
    private long ttl;
    
    private final Map textAttributes = new LinkedHashMap();
    
    private String niceText;
    
    private String[] text;
    
    
    public ServiceInstance(final ServiceName name)
    {
        this.name = name;
    }
    
    
    public ServiceInstance(final ServiceName name, final int priority, final int weight, final int port, final Name host/* , long ttl */, final InetAddress[] addresses, final Collection textRecords)
    {
        this(name, priority, weight, port, host/* , ttl */, addresses, parseTextRecords(textRecords));
    }
    
    
    public ServiceInstance(final ServiceName name, final int priority, final int weight, final int port, final Name host/* , long ttl */, final InetAddress[] addresses, final Map textAttributes)
    {
        super();
        this.name = name;
        this.host = host;
        this.priority = priority;
        this.weight = weight;
        this.port = port;
        ttl = ttl <= 0 ? Constants.DEFAULT_SRV_TTL : ttl;
        
        if (addresses != null)
        {
            this.addresses = new ArrayList(Arrays.asList(addresses));
        }
        
        if (textAttributes != null)
        {
            this.textAttributes.putAll(textAttributes);
            
            text = new String[textAttributes.size()];
            Map.Entry[] entries = (Map.Entry[]) textAttributes.entrySet().toArray(new Map.Entry[textAttributes.size()]);
            for (int index = 0; index < entries.length; index++ )
            {
                text[index] = entries[index].getKey() + "=" + entries[index].getValue();
            }
        }
    }
    
    
    public ServiceInstance(final ServiceName name, final int priority, final int weight, final int port, final Name host/* , long ttl */, final InetAddress[] addresses, final String... textRecords)
    {
        this(name, priority, weight, port, host/* , ttl */, addresses, parseTextRecords(textRecords));
    }
    
    
    public ServiceInstance(final ServiceName name, final int priority, final int weight, final int port, final Name host/* , long ttl */, final InetAddress[] addresses, final TXTRecord... textRecords)
    {
        this(name, priority, weight, port, host/* , ttl */, addresses, parseTextRecords(textRecords));
    }
    
    
    public ServiceInstance(final SRVRecord srv)
    throws TextParseException
    {
        this(new ServiceName(srv.getName()), srv.getPriority(), srv.getWeight(), srv.getPort(), srv.getTarget(), null, (Map) null);
    }
    
    
    public void addAddress(final InetAddress address)
    {
        if (!addresses.contains(address))
        {
            addresses.add(address);
        }
    }
    
    
    public void addPointer(final Name pointer)
    {
        if (!pointers.contains(pointer))
        {
            pointers.add(pointer);
        }
    }
    
    
    public void addText(final Map textRecords)
    {
        if (textRecords != null)
        {
            textAttributes.putAll(textRecords);
        }
    }
    
    
    public void addText(final String name, final String value)
    {
        textAttributes.put(name, value);
    }
    
    
    public void addTextRecords(final TXTRecord... textRecords)
    {
        Map newTextRecords = parseTextRecords(textRecords);
        if (newTextRecords != null)
        {
            textAttributes.putAll(newTextRecords);
        }
    }
    
    
    public InetAddress[] getAddresses()
    {
        return (addresses == null) || (addresses.size() == 0) ? null : (InetAddress[]) addresses.toArray(new InetAddress[addresses.size()]);
    }
    
    
    public Name getHost()
    {
        return host;
    }
    
    
    /*
     * public long getTTL()
     * {
     * return ttl;
     * }
     */
    
    public ServiceName getName()
    {
        return name;
    }
    
    
    public String getNiceText()
    {
        return niceText;
    }
    
    
    public Name[] getPointers()
    {
        return (pointers == null) || (pointers.size() == 0) ? null : (Name[]) pointers.toArray(new Name[pointers.size()]);
    }
    
    
    public int getPort()
    {
        return port;
    }
    
    
    public int getPriority()
    {
        return priority;
    }
    
    
    public String[] getText()
    {
        return text;
    }
    
    
    public Map getTextAttributes()
    {
        return textAttributes;
    }
    
    
    public int getWeight()
    {
        return weight;
    }
    
    
    public void removeAddress(final InetAddress address)
    {
        addresses.remove(address);
    }
    
    
    public void removePointer(final Name pointer)
    {
        pointers.remove(pointer);
    }
    
    
    public void removeTextRecords(final TXTRecord... textRecords)
    {
        Map removedTextRecords = parseTextRecords(textRecords);
        if (removedTextRecords != null)
        {
            for (Object key : removedTextRecords.keySet())
            {
                textAttributes.remove(key);
            }
        }
    }
    
    
    public void setAddresses(final List addresses)
    {
        if (addresses != null)
        {
            this.addresses.clear();
            this.addresses.addAll(addresses);
        }
    }
    
    
    public void setHost(final Name host)
    {
        this.host = host;
    }
    
    
    public void setNiceText(final String niceText)
    {
        this.niceText = niceText;
    }
    
    
    public void setPointers(final List pointers)
    {
        if (pointers != null)
        {
            this.pointers.clear();
            this.pointers.addAll(pointers);
        }
    }
    
    
    public void setPort(final int port)
    {
        this.port = port;
    }
    
    
    public void setPriority(final int priority)
    {
        this.priority = priority;
    }
    
    
    public void setWeight(final int weight)
    {
        this.weight = weight;
    }
    
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Service (\"").append(name).append("\"");
        
        if (host != null)
        {
            builder.append(" can be reached at \"").append(host).append("\" ").append(Arrays.toString(getAddresses()));
        }
        
        if (port > 0)
        {
            builder.append(" on port ").append(getPort());
        }
        
        StringBuilder textBuilder = new StringBuilder();
        if ((textAttributes != null) && (textAttributes.size() > 0))
        {
            for (Object o : textAttributes.entrySet())
            {
                Map.Entry entry = (Map.Entry) o;
                if (textBuilder.length() == 0)
                {
                    builder.append("\n\tTXT: ");
                }
                
                textBuilder.append(entry.getKey());
                Object value = entry.getValue();
                if (value != null)
                {
                    textBuilder.append("=\"").append(value.toString()).append("\"");
                }
                textBuilder.append(", ");
                
                if (textBuilder.length() > 100)
                {
                    textBuilder.setLength(builder.length() - 2); // Trim trailing comma
                    builder.append(textBuilder);
                    textBuilder.setLength(0);
                }
            }
            
            if (textBuilder.length() > 0)
            {
                textBuilder.setLength(builder.length() - 2); // Trim trailing comma
                builder.append(textBuilder);
                textBuilder.setLength(0);
            }
        }
        
        builder.append(")");
        return builder.toString();
    }
    
    
    /**
     * Adds the specified text to the service.
     * 
     * Accepts an Object that is either a String, TXTRecord, or an objects who's string
     * representation is a string of name/value pairs, in the format
     * 
     * [WS] NAME [WS] "=" [WS] VALUE WS *([WS] NAME [WS] "=" [WS] VALUE [WS]) (NULL | NEWLINE)
     * 
     * @param rawText
     * @return
     */
    protected static Map parseTextRecords(final Object rawText)
    {
        if (rawText == null)
        {
            return null;
        } else if (rawText instanceof Map)
        {
            return (Map) rawText;
        } else if (rawText.getClass().isArray())
        {
            Map textAttributes = new LinkedHashMap();
            Object[] array = (Object[]) rawText;
            
            if ((array != null) && (array.length > 0))
            {
                textAttributes = new LinkedHashMap();
                for (int index = 0; index < array.length; index++ )
                {
                    Map map = parseTextRecords(array[index]);
                    if ((map != null) && (map.size() > 0))
                    {
                        textAttributes.putAll(map);
                    }
                }
            }
            return textAttributes;
        } else if (rawText instanceof Collection)
        {
            return parseTextRecords(((Collection) rawText).toArray());
        } else if (rawText instanceof TXTRecord)
        {
            return parseTextRecords(((TXTRecord) rawText).getStrings().toArray());
        } else
        {
            Map textAttributes = new LinkedHashMap();
            String[] pairs = split(rawText.toString());
            for (String pair : pairs)
            {
                if ((pair != null) && (pair.length() > 0))
                {
                    String key = "";
                    String value = "";
                    
                    int index = pair.indexOf('=');
                    if (index >= 0)
                    {
                        key = pair.substring(0, index);
                        index++ ;
                        if (index <= pair.length())
                        {
                            value = pair.substring(index);
                        }
                    } else
                    {
                        key = pair;
                    }
                    
                    textAttributes.put(key, value);
                }
            }
            
            return textAttributes;
        }
    }
    
    
    private static String[] split(final String text)
    {
        ArrayList list = new ArrayList();
        StringBuilder builder = new StringBuilder();
        
        boolean inQuote = false;
        boolean escape = false;
        char[] chars = (text + '\n').toCharArray();
        
        for (int index = 0; index < chars.length; index++ )
        {
            if (!Character.isWhitespace(chars[index]))
            {
                switch (chars[index])
                {
                    case '\\':
                        escape = true;
                        break;
                    case '\"':
                        if (!escape)
                        {
                            inQuote = !inQuote;
                            break;
                        } else
                        {
                            builder.append(chars[index]);
                        }
                        break;
                    default:
                        builder.append(chars[index]);
                        if (escape)
                        {
                            escape = false;
                        }
                        break;
                }
            } else
            {
                list.add(builder.toString());
                builder.setLength(0);
            }
        }
        
        return (String[]) list.toArray(new String[list.size()]);
    }
}
