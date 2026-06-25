using Mirror;
using UnityEngine;
using UnityEngine.InputSystem;
using UnityEngine.UI;

public class PauseMenu : MonoBehaviour
{
    private bool isPaused;
    public bool IsPaused
    {
        get { return isPaused; }
        private set { isPaused = value; }
    }

    public GameObject panelRoot;
    public GameObject instructionsRoot;
    public Text scoreboardText;

    public Button resumeButton;
    public Button instructionsButton;
    public Button quitButton;

    public InputActionReference pauseAction;

    public void Start()
    {
        ShowPanel(false);
    }

    public void OnEnable()
    {
        if (pauseAction != null && pauseAction.action != null)
        {
            pauseAction.action.performed += OnPausePressed;
            pauseAction.action.Enable();
        }
    }

    public void OnDisable()
    {
        if (pauseAction != null && pauseAction.action != null)
        {
            pauseAction.action.performed -= OnPausePressed;
        }
    }

    public void OnPausePressed(InputAction.CallbackContext context)
    {
        TogglePause();
    }

    public void Update()
    {
        if (IsPaused && scoreboardText)
        {
            NetworkRaceManager manager = NetworkRaceManager.instance;
            if (manager)
            {
                scoreboardText.text = manager.rankingText;
            }
            else
            {
                scoreboardText.text = "Waiting for race...";
            }
        }
    }

    public void TogglePause()
    {
        IsPaused = !IsPaused;
        ShowPanel(IsPaused);
    }

    public void ShowPanel(bool show)
    {
        if (panelRoot)
        {
            panelRoot.SetActive(show);
        }

        if (instructionsRoot)
        {
            instructionsRoot.SetActive(false);
        }

        if (show)
        {
            Cursor.lockState = CursorLockMode.None;
        }
        else
        {
            Cursor.lockState = CursorLockMode.Locked;
        }

        Cursor.visible = show;
    }

    public void ShowInstructions()
    {
        if (instructionsRoot)
        {
            instructionsRoot.SetActive(!instructionsRoot.activeSelf);
        }
    }

    public void QuitToDesktop()
    {
        if (NetworkServer.active && NetworkClient.isConnected)
        {
            NetworkManager.singleton.StopHost();
        }
        else if (NetworkClient.isConnected)
        {
            NetworkManager.singleton.StopClient();
        }

        Application.Quit();
    }
}
