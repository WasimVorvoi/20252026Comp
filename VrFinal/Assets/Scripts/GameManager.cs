using UnityEngine;
using TMPro;

public class GameManager : MonoBehaviour
{
    public int score;
    public int startScore;
    public int minScore;
    public float gameLength;
    public float timeLeft;
    public bool gameRunning;

    public string startMessage;
    public string emptyMessage;
    public string gameOverMessage;
    public string scorePrefix;
    public string timePrefix;

    public TMP_Text timerText;
    public TMP_Text messageText;

    public TMP_Text camScoreText;
    public TMP_Text difficultyText;

    public FruitSpawner spawner;
    public HighScores highScores;

    public float slowMoScale;
    public float slowMoDuration;
    private bool slowMoActive;
    private float slowMoTimer;

    public float doublePointsDuration;
    private bool doublePointsActive;
    private float doublePointsTimer;

    public Flashbang flashbang;

    public int difficultyLevel;

    void Start()
    {
        Transform camCanvas = Camera.main.transform.Find("Canvas");

        score = startScore;
        timeLeft = gameLength;
        gameRunning = false;
        slowMoActive = false;
        doublePointsActive = false;
        difficultyLevel = 1;
        UpdateDifficultyText();
        messageText.text = startMessage;
        timerText.text = timePrefix + Mathf.CeilToInt(gameLength);
        camScoreText.text = scorePrefix + score;
    }

void Update()
    {
        if (gameRunning == true)
        {
            timeLeft = timeLeft - Time.deltaTime;
            timerText.text = timePrefix + Mathf.CeilToInt(timeLeft);
            if (timeLeft <= 0)
            {
                EndGame();
            }
        }

        if (slowMoActive == true)
        {
            slowMoTimer = slowMoTimer - Time.unscaledDeltaTime;
            if (slowMoTimer <= 0)
            {
                slowMoActive = false;
                Time.timeScale = 1f;
            }
        }

        if (doublePointsActive == true)
        {
            doublePointsTimer = doublePointsTimer - Time.deltaTime;
            if (doublePointsTimer <= 0)
            {
                doublePointsActive = false;
            }
        }
    }

    public void TriggerSlowMo()
    {
        slowMoActive = true;
        slowMoTimer = slowMoDuration;
        Time.timeScale = slowMoScale;
    }

    public void TriggerDoublePoints()
    {
        doublePointsActive = true;
        doublePointsTimer = doublePointsDuration;
    }

    public void TriggerFlashbang()
    {
        flashbang.Trigger();
    }

    public void StartGame()
    {
        if (gameRunning == true)
        {
            return;
        }
        score = startScore;
        timeLeft = gameLength;
        gameRunning = true;
        difficultyLevel = 1;
        spawner.SetDifficulty(1);
        UpdateDifficultyText();
        messageText.text = emptyMessage;
        spawner.spawning = true;
    }

    public void EndGame()
    {
        gameRunning = false;
        timeLeft = 0;
        spawner.spawning = false;
        highScores.TrySaveScore(score);
        messageText.text = gameOverMessage + score;
    }

    public void AddScore(int amount)
    {
        if (doublePointsActive && amount > 0)
            amount *= 2;
        score = score + amount;
        if (score < minScore)
            score = minScore;
        camScoreText.text = scorePrefix + score;
        CheckDifficulty();
    }


    void CheckDifficulty()
    {
        int newLevel = 1;
        if (score >= 5) newLevel = 2;
        if (score >= 12) newLevel = 3;
        if (score >= 22) newLevel = 4;
        if (score >= 35) newLevel = 5;
        if (score >= 50) newLevel = 6;

        if (newLevel != difficultyLevel)
        {
            difficultyLevel = newLevel;
            spawner.SetDifficulty(difficultyLevel);
            UpdateDifficultyText();
        }
    }

    void UpdateDifficultyText()
    {
        difficultyText.text = "Difficulty " + difficultyLevel;
    }
}
