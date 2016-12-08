package nl.davinci.davinciquest.Entity;

/**
 * Created by Vincent on 8-12-2016.
 */

public class Highscore
{
    private int id;
    private int score;
    private int userId;
    private int questId;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public int getQuestId()
    {
        return questId;
    }

    public void setQuestId(int questId)
    {
        this.questId = questId;
    }
}
