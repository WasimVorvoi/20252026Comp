using UnityEngine;

public class CheckpointManager : MonoBehaviour
{
    public static CheckpointManager instance;

    public int checkpointCount = 10;

    public void Start()
    {
        instance = this;
    }

    public int GetCheckpointCount()
    {
        return checkpointCount;
    }
}
