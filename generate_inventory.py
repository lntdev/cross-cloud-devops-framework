# import json

# with open("tf_outputs.json") as f:
#     tf = json.load(f)

# master_ip = tf["eks_master_ip"]["value"]
# worker_ips = tf["eks_worker_ips"]["value"]

# with open("ansible/inventory/aws_hosts.ini", "w") as f:
#     f.write("[masters]\n")
#     f.write(master_ip + "\n\n")
#     f.write("[workers]\n")
#     for ip in worker_ips:
#         f.write(ip + "\n")

# import json

# with open("tf_outputs.json") as f:
#     tf = json.load(f)

# # Adjust the keys to match your Terraform output
# master_ips = tf["master_ips"]["value"]      # Could be "masters" or "eks_master" depending on your output
# worker_ips = tf["worker_ips"]["value"]      # Could be "eks_workers"

# with open("ansible/inventory/aws_hosts.ini", "w") as f:
#     f.write("[masters]\n")
#     for ip in master_ips:
#         f.write(ip + "\n")
#     f.write("\n[workers]\n")
#     for ip in worker_ips:
#         f.write(ip + "\n")

#!/usr/bin/env python3
import json
import argparse
import os
import sys
from typing import Any, List

DEFAULT_TF_JSON = "tf_outputs.json"
DEFAULT_INV_PATH = os.path.join("ansible", "inventory", "aws_hosts.ini")

MASTER_KEY_CANDIDATES = [
    "master_ips", "masters", "eks_master_ip", "eks_master_ips",
    "control_plane_ips", "control_plane_private_ips", "control_plane_public_ips"
]

WORKER_KEY_CANDIDATES = [
    "worker_ips", "workers", "eks_worker_ips", "node_group_private_ips",
    "node_group_public_ips", "eks_node_ips"
]

EC2_KEY_CANDIDATES = [
    "ec2_instance_ips", "extra_ec2_public_ip", "extra_ec2_private_ip",
    "ec2_public_ips", "ec2_private_ips"
]

def normalize_to_list(v: Any) -> List[str]:
    """
    Terraform outputs may be:
      - {"value": ["1.2.3.4", "1.2.3.5"]}
      - {"value": "1.2.3.4"}
      - {"value": {"ip1": "1.2.3.4", "ip2": "1.2.3.5"}}
    Convert to a flat list of strings.
    """
    if isinstance(v, list):
        return [str(x) for x in v]
    if isinstance(v, str):
        return [v] if v.strip() else []
    if isinstance(v, dict):
        # collect any string-ish values
        out = []
        for _, val in v.items():
            if isinstance(val, (str, int, float)):
                out.append(str(val))
            elif isinstance(val, list):
                out.extend([str(x) for x in val])
        return out
    return []

def get_value(tf: dict, key: str) -> List[str]:
    if key not in tf:
        return []
    obj = tf[key]
    if isinstance(obj, dict) and "value" in obj:
        return normalize_to_list(obj["value"])
    # fallback if someone piped raw values
    return normalize_to_list(obj)

def find_first_match(tf: dict, candidates: List[str]) -> (str, List[str]):
    for k in candidates:
        vals = get_value(tf, k)
        if vals:
            return k, vals
    return "", []

def main():
    p = argparse.ArgumentParser(description="Generate Ansible inventory from Terraform outputs.")
    p.add_argument("--tf-json", default=DEFAULT_TF_JSON, help="Path to terraform output -json file.")
    p.add_argument("--out", default=DEFAULT_INV_PATH, help="Path to write inventory (ini).")
    p.add_argument("--masters-key", help="Explicit key in tf json to read master IPs.")
    p.add_argument("--workers-key", help="Explicit key in tf json to read worker IPs.")
    p.add_argument("--ec2-key", help="Explicit key for generic EC2 IPs (added to [ec2] group).")
    args = p.parse_args()

    if not os.path.exists(args.tf_json):
        print(f"[ERROR] Terraform outputs file not found: {args.tf_json}", file=sys.stderr)
        sys.exit(2)

    with open(args.tf_json) as f:
        try:
            tf = json.load(f)
        except Exception as e:
            print(f"[ERROR] Failed to parse JSON: {e}", file=sys.stderr)
            sys.exit(2)

    # Resolve keys
    masters_key = args.masters_key
    workers_key = args.workers_key
    ec2_key     = args.ec2_key

    masters = get_value(tf, masters_key) if masters_key else []
    workers = get_value(tf, workers_key) if workers_key else []
    ec2     = get_value(tf, ec2_key)     if ec2_key     else []

    if not masters and not masters_key:
        masters_key, masters = find_first_match(tf, MASTER_KEY_CANDIDATES)
    if not workers and not workers_key:
        workers_key, workers = find_first_match(tf, WORKER_KEY_CANDIDATES)
    if not ec2 and not ec2_key:
        ec2_key, ec2 = find_first_match(tf, EC2_KEY_CANDIDATES)

    # Prepare inventory dirs
    os.makedirs(os.path.dirname(args.out), exist_ok=True)

    lines = []
    lines.append("[masters]")
    lines.extend(masters or [])
    lines.append("")  # blank line

    lines.append("[workers]")
    lines.extend(workers or [])
    lines.append("")

    # Optional generic EC2 group
    if ec2:
        lines.append("[ec2]")
        lines.extend(ec2)
        lines.append("")

    # Write the file
    with open(args.out, "w") as f:
        f.write("\n".join(lines).rstrip() + "\n")

    print(f"[OK] Inventory written to {args.out}")
    if masters_key:
        print(f"  masters <- {masters_key}: {masters or '[]'}")
    else:
        if masters:
            print(f"  masters <- {masters_key or 'auto-detected'}: {masters}")
        else:
            print("  masters: (none found)")

    if workers_key:
        print(f"  workers <- {workers_key}: {workers or '[]'}")
    else:
        if workers:
            print(f"  workers <- {workers_key or 'auto-detected'}: {workers}")
        else:
            print("  workers: (none found)")

    if ec2:
        print(f"  ec2     <- {ec2_key or 'auto-detected'}: {ec2}")

    # Guidance if nothing found
    if not masters and not workers and not ec2:
        print(
            "\n[INFO] No IPs found in tf_outputs.json.\n"
            "- This is normal for EKS managed node groups (AWS doesn't expose node IPs as outputs).\n"
            "- If you need inventory for standalone EC2s, add Terraform outputs like `ec2_public_ips` and rerun.\n"
            "- Or pass explicit keys with --masters-key / --workers-key / --ec2-key.\n"
            f"An empty inventory template was still written to {args.out}.\n"
        )

if __name__ == "__main__":
    main()
