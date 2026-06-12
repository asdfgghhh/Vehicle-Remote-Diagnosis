#!/usr/bin/env python3
"""Generate a DBC file with N controllers (ECU nodes) and M signals."""

import argparse
from pathlib import Path

# Common automotive ECU names as controllers
DEFAULT_ECUS = [
    "EMS",   # Engine Management
    "BCM",   # Body Control Module
    "ABS",   # Anti-lock Braking
    "EPS",   # Electric Power Steering
    "IPC",   # Instrument Panel Cluster
    "ADAS",  # Advanced Driver Assistance
    "TBOX",  # Telematics Box
    "GW",    # Gateway
    "TCU",   # Transmission Control
    "BMS",   # Battery Management
]


def generate_dbc(ecus: list[str], total_signals: int) -> str:
    if len(ecus) == 0:
        raise ValueError("At least one ECU required")
    if total_signals % len(ecus) != 0:
        raise ValueError("total_signals must be divisible by number of ECUs")

    signals_per_ecu = total_signals // len(ecus)
    max_signals_per_msg = 8  # 8-byte classic CAN frame, 8-bit signals

    lines: list[str] = [
        'VERSION "1.0"',
        "",
        "",
        "NS_ :",
        "    NS_DESC_",
        "    CM_",
        "    BA_DEF_",
        "    BA_",
        "    VAL_",
        "    CAT_DEF_",
        "    CAT_",
        "    FILTER",
        "    BA_DEF_DEF_",
        "    EV_DATA_",
        "    ENVVAR_DATA_",
        "    SGTYPE_",
        "    SGTYPE_VAL_",
        "    BA_DEF_SGTYPE_",
        "    BA_SGTYPE_",
        "    SIG_TYPE_REF_",
        "    VAL_TABLE_",
        "    SIG_GROUP_",
        "    SIG_VALTYPE_",
        "    SIGTYPE_VALTYPE_",
        "    BO_TX_BU_",
        "    BA_DEF_REL_",
        "    BA_REL_",
        "    BA_DEF_DEF_REL_",
        "    BU_SG_REL_",
        "    BU_EV_REL_",
        "    BU_BO_REL_",
        "    SG_MUL_VAL_",
        "",
        "BS_:",
        "",
        f"BU_: {' '.join(ecus)}",
        "",
    ]

    signal_index = 0
    msg_count = 0
    msg_cycles: list[tuple[int, int]] = []
    sig_samples: list[tuple[int, str, int]] = []
    cycle_options = [10, 20, 50, 100, 200, 500, 1000]

    for ecu_idx, ecu in enumerate(ecus):
        base_id = 0x200 + ecu_idx * 0x80
        remaining = signals_per_ecu
        msg_idx = 0
        while remaining > 0:
            sig_count = min(max_signals_per_msg, remaining)
            msg_id = base_id + msg_idx * 8
            msg_name = f"{ecu}_Msg{msg_idx + 1:02d}"
            msg_cycle_ms = cycle_options[(ecu_idx + msg_idx) % len(cycle_options)]
            msg_cycles.append((msg_id, msg_cycle_ms))
            lines.append(f"BO_ {msg_id} {msg_name}: 8 {ecu}")

            for sig_idx in range(sig_count):
                signal_index += 1
                global_sig_num = signal_index
                sig_name = f"{ecu}_Sig{global_sig_num:03d}"
                start_bit = sig_idx * 8
                # 部分信号单独配置采样周期，其余继承报文周期
                if sig_idx == 0:
                    sig_samples.append((msg_id, sig_name, max(10, msg_cycle_ms // 2)))
                lines.append(
                    f' SG_ {sig_name} : {start_bit}|8@1+ (1,0) [0|255] "" {ecu}'
                )

            lines.append("")
            remaining -= sig_count
            msg_idx += 1
            msg_count += 1

    assert signal_index == total_signals, f"Expected {total_signals}, got {signal_index}"

    lines.extend([
        f'CM_ "Generated DBC: {len(ecus)} controllers, {total_signals} signals";',
        "",
        'BA_DEF_ BO_  "GenMsgCycleTime" INT 0 60000;',
        'BA_DEF_ SG_  "SamplePeriod" INT 0 60000;',
        "",
    ])

    for msg_id, cycle_ms in msg_cycles:
        lines.append(f'BA_ "GenMsgCycleTime" BO_ {msg_id} {cycle_ms};')
    lines.append("")
    for msg_id, sig_name, sample_ms in sig_samples:
        lines.append(f'BA_ "SamplePeriod" SG_ {msg_id} {sig_name} {sample_ms};')
    lines.extend([
        "",
        'BA_DEF_ SG_  "GenComment" STRING;',
        'BA_ "GenComment" SG_ 0 "Auto-generated for Vehicle Remote Diagnosis";',
        "",
    ])

    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate sample DBC file")
    parser.add_argument(
        "-o",
        "--output",
        default="data/dbc/vrd_10ecu_500sig.dbc",
        help="Output file path",
    )
    parser.add_argument("--ecus", type=int, default=10, help="Number of controllers")
    parser.add_argument("--signals", type=int, default=500, help="Total signal count")
    args = parser.parse_args()

    ecus = DEFAULT_ECUS[: args.ecus]
    if len(ecus) < args.ecus:
        ecus.extend(f"ECU{i + 1}" for i in range(len(ecus), args.ecus))

    content = generate_dbc(ecus, args.signals)
    out = Path(args.output)
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(content, encoding="utf-8")

    print(f"Generated: {out}")
    print(f"  Controllers (BU_): {args.ecus}")
    print(f"  Messages (BO_):    {content.count(chr(10) + 'BO_ ')}")
    print(f"  Signals (SG_):     {args.signals}")


if __name__ == "__main__":
    main()
