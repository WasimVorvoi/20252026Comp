using UnityEngine;

public class Checkpoint : MonoBehaviour
{
    public int checkpointIndex;
    public bool isFinishLine;

    public void OnTriggerEnter(Collider other)
    {
        RacePlayer player = other.GetComponentInParent<RacePlayer>();
        if (player)
        {
            player.ServerCheckpointHit(checkpointIndex);
        }
    }
}
