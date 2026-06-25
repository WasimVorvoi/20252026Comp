using UnityEngine;
using Mirror;

public class DojoPlayer : NetworkBehaviour
{
    public Behaviour[] localOnly;

    void Start()
    {
        if (isLocalPlayer == false)
        {
            for (int i = 0; i < localOnly.Length; i++)
            {
                localOnly[i].enabled = false;
            }
        }
    }
}
