package com.desiremc.hcf.session;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IdGetter;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

import com.desiremc.core.DesireCore;
import com.desiremc.core.scoreboard.EntryRegistry;
import com.desiremc.core.session.DeathBan;
import com.desiremc.core.session.DeathBanHandler;
import com.desiremc.core.session.PVPClass;
import com.desiremc.core.session.Rank;
import com.desiremc.core.session.Session;
import com.desiremc.core.session.Ticker;
import com.desiremc.core.utils.PlayerUtils;

@Entity(value = "hcf_sessions", noClassnameStored = true)
public class HCFSession
{

    @Transient
    private static final boolean DEBUG = false;

    @Id
    private int id;

    @Indexed
    private UUID uuid;

    @Indexed
    private String server;

    @Property("safe_timer")
    private long safeTimer;

    private int lives;

    private double balance;

    @Reference(ignoreMissing = true)
    private List<DeathBan> deathBans;

    @Embedded
    private List<Ticker> kills;

    @Embedded
    private List<Ticker> deaths;

    @Embedded
    private OreData currentOre;

    private Map<Integer, Integer> kitUses;

    private Map<Integer, Long> kitCooldowns;

    @Transient
    private Session session;

    @Transient
    private PVPTimer pvpTimer;

    @Transient
    private Player player;

    @Transient
    private PVPClass pvpClass;

    public HCFSession()
    {
        pvpTimer = new PVPTimer();
        kills = new LinkedList<>();
        deaths = new LinkedList<>();
        deathBans = new LinkedList<>();
        kitUses = new HashMap<>();
    }

    protected void setId(int id)
    {
        this.id = id;
    }

    @IdGetter
    protected int getId()
    {
        return id;
    }

    protected void assignDefault(UUID uuid, String server)
    {
        this.id = HCFSessionHandler.getNextId();
        this.uuid = uuid;
        this.server = server;
        this.safeTimer = DesireCore.getConfigHandler().getInteger("timers.pvp.time") * 1000;
    }

    public Rank getRank()
    {
        return session.getRank();
    }

    public Player getPlayer()
    {
        if (player == null)
        {
            player = PlayerUtils.getPlayer(uuid);
        }
        return player;
    }

    @IdGetter
    public UUID getUniqueId()
    {
        return uuid;
    }

    protected void setUniqueId(UUID uuid)
    {
        this.uuid = uuid;
    }

    public String getName()
    {
        return session.getName();
    }

    public long getSafeTimeLeft()
    {
        return safeTimer;
    }

    public void setSafeTimeLeft(int safeTimer)
    {
        this.safeTimer = safeTimer;
        save();
    }

    public int getLives()
    {
        return lives;
    }

    public void setLives(int lives)
    {
        this.lives = lives;
        save();
    }

    public void takeLives(int lives)
    {
        this.lives -= lives;
        save();
    }

    public void addLives(int lives)
    {
        this.lives += lives;
        save();
    }

    public double getBalance()
    {
        return balance;
    }

    public void setBalance(double balance)
    {
        this.balance = balance;
        save();
    }

    public void depositBalance(double amount)
    {
        this.balance += amount;
        save();
    }

    public void withdrawBalance(double amount)
    {
        this.balance -= amount;
        save();
    }

    public int getTotalKills()
    {
        int count = 0;
        for (Ticker ticker : kills)
        {
            count += ticker.getCount();
        }
        return count;
    }

    public int getTotalDeaths()
    {
        int count = 0;
        for (Ticker ticker : deaths)
        {
            count += ticker.getCount();
        }
        return count;
    }

    public void addKill(UUID victim)
    {
        Ticker tick = null;
        for (Ticker t : kills)
        {
            if (t.getUniqueId().equals(victim))
            {
                tick = t;
            }
        }
        if (tick == null)
        {
            tick = new Ticker(victim);
            kills.add(tick);
        }
        else
        {
            tick.setCount(tick.getCount() + 1);
        }
    }

    public void addDeath(UUID killer)
    {
        if (DEBUG)
        {
            System.out.println("addDeath() called with server " + server + " and killer " + (killer == null ? "null" : killer.toString()) + ".");
        }

        deathBans.add(DeathBanHandler.createDeathBan(getUniqueId()));

        if (killer != null)
        {
            Ticker tick = null;
            for (Ticker t : deaths)
            {
                if (t.getUniqueId().equals(killer))
                {
                    tick = t;
                }
            }
            if (tick == null)
            {
                tick = new Ticker(killer);
                deaths.add(tick);
            }
            else
            {
                tick.setCount(tick.getCount() + 1);
            }
            deaths.add(new Ticker(killer));
        }
    }

    public int getTokens()
    {
        return session.getTokens();
    }

    public void sendMessage(String message)
    {
        getPlayer().sendMessage(message);
    }

    public PVPTimer getSafeTimer()
    {
        return pvpTimer;
    }

    public boolean hasDeathBan()
    {
        return getActiveDeathBan() != null;
    }

    public long getDeathBanEnd()
    {
        DeathBan ban = getActiveDeathBan();
        return ban != null ? ban.getStartTime() : -1;
    }

    public void revive(String reson, boolean staffRevived, UUID reviver)
    {
        DeathBan ban = getActiveDeathBan();
        if (ban == null)
        {
            throw new IllegalStateException("Player does not have a deathban.");
        }
        ban.setRevived(true);
        ban.setStaffRevive(staffRevived);
        ban.setReviveReason(reson);
        ban.setReviver(reviver);
        ban.save();
        save();
    }

    public void resetPVPTimer()
    {
        pvpTimer = new PVPTimer();
        safeTimer = DesireCore.getConfigHandler().getInteger("timers.pvp.time") * 1000;
    }

    public DeathBan getActiveDeathBan()
    {
        if (DEBUG)
        {
            System.out.println("getActiveDeathBan() called.");
            System.out.println("getActiveDeathBan() rank time = " + session.getRank().getDeathBanTime());
        }
        for (DeathBan ban : deathBans)
        {
            if (DEBUG)
            {
                System.out.println("getActiveDeathBan() loop with values " + ban.getStartTime() + " and " + ban.isRevived());
                System.out.println("getActiveDeathBan() times = " + (ban.getStartTime() + session.getRank().getDeathBanTime()) + " vs " + System.currentTimeMillis());
            }
            if (!ban.wasStaffRevived() && !ban.isRevived() && ban.getStartTime() + session.getRank().getDeathBanTime() > System.currentTimeMillis())
            {
                if (DEBUG)
                {
                    System.out.println("getActiveDeathBan() returned ban.");
                }
                return ban;
            }
        }
        if (DEBUG)
        {
            System.out.println("getActiveDeathBan() returned null.");
        }
        return null;
    }

    public void setSession(Session session)
    {
        this.session = session;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof HCFSession))
        {
            return false;
        }
        return ((HCFSession) o).getUniqueId().equals(uuid);
    }

    public String[] getKillDisplay()
    {
        Collections.sort(kills);
        String[] array = new String[kills.size()];
        int i = 0;
        for (Ticker tick : kills)
        {
            array[i] = PlayerUtils.getName(tick.getUniqueId()) + " x" + tick.getCount();
        }
        return array;
    }

    private static final DecimalFormat df = new DecimalFormat("00");

    public class PVPTimer implements Runnable
    {

        private long lastRunTime;

        private boolean paused;

        @Override
        public void run()
        {
            if (!paused && safeTimer > 0)
            {
                Bukkit.getScheduler().runTaskLater(DesireCore.getInstance(), this, 5);
            }
            if (DEBUG)
            {
                System.out.println("PVPTimer.run() safeTimer = " + safeTimer);
            }
            safeTimer -= System.currentTimeMillis() - lastRunTime;
            lastRunTime = System.currentTimeMillis();
            if (safeTimer <= 0)
            {
                EntryRegistry.getInstance().removeValue(getPlayer(), DesireCore.getLangHandler().getStringNoPrefix("pvp.scoreboard"));
                safeTimer = 0;
            }
            else
            {
                setScoreboard();
            }
        }

        public void setScoreboard()
        {
            EntryRegistry.getInstance().setValue(getPlayer(), DesireCore.getLangHandler().getStringNoPrefix("pvp.scoreboard"), getTimeLeftFormatted());
        }

        public String getTimeLeftFormatted()
        {
            long time = (safeTimer / 1000);
            return "§b" + df.format(time / 60) + ":" + df.format(time % 60);
        }

        public void pause()
        {
            paused = true;
        }

        public void resume()
        {
            lastRunTime = System.currentTimeMillis();
            paused = false;
            run();
        }

    }

    public PVPClass getPvpClass()
    {
        return pvpClass;
    }

    public void setPvpClass(PVPClass pvpClass)
    {
        this.pvpClass = pvpClass;
        save();
    }

    public OreData getCurrentOre()
    {
        if (currentOre == null)
        {
            currentOre = new OreData();
        }
        return currentOre;
    }

    public void save()
    {
        HCFSessionHandler.getInstance().save(this);
    }

    public void useKit(HKit kit)
    {
        Integer val = kitUses.get(kit.getId());
        if (val == null)
        {
            val = 1;
        }
        kitUses.put(kit.getId(), val);
        kitCooldowns.put(kit.getId(), System.currentTimeMillis());
    }

    public long getKitCooldown(HKit kit)
    {
        Long val = kitCooldowns.get(kit.getId());

        if (val == null)
        {
            return 0;
        }

        return Long.max(0, (kit.getCooldown() * 1000) - (System.currentTimeMillis() - val));
    }

    public boolean hasKitCooldown(HKit kit)
    {
        return getKitCooldown(kit) > 0;
    }
}
