using TMPro;
using UnityEngine;
using UnityEngine.SceneManagement;

public class Score : MonoBehaviour
{
    public TMP_Text scoreText;
    public TMP_Text timerDisplay;
    public TMP_Text highscore1Text;
    public TMP_Text highscore2Text;
    public TMP_Text highscore3Text;
    public TMP_Text finalScoreText;
    public GameObject gameOverPanel;
    public Spawn spawner;

    public float timeRemaining = 60;
    public bool gameOver = false;
    bool gameStarted = false;

    int playerScore;
    int highscore1;
    int highscore2;
    int highscore3;

    void Start()
    {
        playerScore = 0;
        highscore1 = PlayerPrefs.GetInt("highScore1", 0);
        highscore2 = PlayerPrefs.GetInt("highScore2", 0);
        highscore3 = PlayerPrefs.GetInt("highScore3", 0);
        gameOverPanel.SetActive(false);
        UpdateScore();
        UpdateDisplay(timeRemaining);
    }

    public void StartGame()
    {
        if (gameStarted) return;
        gameStarted = true;
        spawner.BeginSpawning();
    }

    void Update()
    {
        if (!gameStarted || gameOver) return;

        if (timeRemaining > 0)
        {
            timeRemaining -= Time.deltaTime;
            UpdateDisplay(timeRemaining);
        }
        else
        {
            timeRemaining = 0;
            EndGame();
        }
    }

    void EndGame()
    {
        gameOver = true;
        spawner.StopSpawning();
        UpdateHighScores(playerScore);
        finalScoreText.text = "Final Score: " + playerScore;
        gameOverPanel.SetActive(true);
    }

    public void AddPoints(int amount)
    {
        if (gameOver) return;
        playerScore += amount;
        if (playerScore < 0) playerScore = 0;
        UpdateScore();
    }

    public void Restart()
    {
        SceneManager.LoadScene(SceneManager.GetActiveScene().name);
    }

    void UpdateDisplay(float timeToDisplay)
    {
        float minutes = Mathf.FloorToInt(timeToDisplay / 60);
        float seconds = Mathf.FloorToInt(timeToDisplay % 60);
        timerDisplay.text = string.Format("{0:00}:{1:00}", minutes, seconds);
    }

    void UpdateHighScores(int newScore)
    {
        if (newScore > highscore1)
        {
            highscore3 = highscore2;
            highscore2 = highscore1;
            highscore1 = newScore;
        }
        else if (newScore > highscore2)
        {
            highscore3 = highscore2;
            highscore2 = newScore;
        }
        else if (newScore > highscore3)
        {
            highscore3 = newScore;
        }

        PlayerPrefs.SetInt("highScore1", highscore1);
        PlayerPrefs.SetInt("highScore2", highscore2);
        PlayerPrefs.SetInt("highScore3", highscore3);
        UpdateScore();
    }

    void UpdateScore()
    {
        scoreText.text = "Score: " + playerScore;
        highscore1Text.text = "1st: " + highscore1;
        highscore2Text.text = "2nd: " + highscore2;
        highscore3Text.text = "3rd: " + highscore3;
    }
}
