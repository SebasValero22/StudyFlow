use std::f32::consts::PI;
use std::io::Cursor;
use rodio::{Decoder, OutputStream, Sink};
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub enum ChimeType {
    FocusComplete,
    BreakComplete,
    SessionStart,
}

pub fn synthesize_chime(kind: &ChimeType) -> Vec<u8> {
    const SAMPLE_RATE: u32 = 22050;
    let tones: &[(f32, f32)] = match kind {
        ChimeType::FocusComplete => &[(880.0, 0.22), (1174.7, 0.30)],
        ChimeType::BreakComplete => &[(660.0, 0.22), (880.0, 0.30)],
        ChimeType::SessionStart => &[(523.25, 0.15)],
    };

    let mut samples: Vec<u8> = Vec::new();
    for &(freq, secs) in tones {
        let n = (SAMPLE_RATE as f32 * secs) as usize;
        for i in 0..n {
            let t = i as f32 / SAMPLE_RATE as f32;
            let envelope = (-3.0 * t / secs).exp();
            let v = (f32::sin(2.0 * PI * freq * t) * 100.0 * envelope) as i32;
            samples.push((128 + v.clamp(-127, 127)) as u8);
        }
        samples.extend(std::iter::repeat(128).take((SAMPLE_RATE as f32 * 0.04) as usize));
    }

    let data_len = samples.len() as u32;
    let mut wav = Vec::with_capacity(44 + samples.len());
    wav.extend_from_slice(b"RIFF");
    wav.extend_from_slice(&(36 + data_len).to_le_bytes());
    wav.extend_from_slice(b"WAVE");
    wav.extend_from_slice(b"fmt ");
    wav.extend_from_slice(&16u32.to_le_bytes());
    wav.extend_from_slice(&1u16.to_le_bytes()); // PCM
    wav.extend_from_slice(&1u16.to_le_bytes()); // mono
    wav.extend_from_slice(&SAMPLE_RATE.to_le_bytes());
    wav.extend_from_slice(&SAMPLE_RATE.to_le_bytes()); // byte rate
    wav.extend_from_slice(&1u16.to_le_bytes()); // block align
    wav.extend_from_slice(&8u16.to_le_bytes()); // bits per sample
    wav.extend_from_slice(b"data");
    wav.extend_from_slice(&data_len.to_le_bytes());
    wav.extend_from_slice(&samples);
    wav
}

pub fn play_chime(kind: ChimeType) {
    std::thread::spawn(move || {
        let wav = synthesize_chime(&kind);
        if let Ok((_stream, stream_handle)) = OutputStream::try_default() {
            if let Ok(sink) = Sink::try_new(&stream_handle) {
                if let Ok(source) = Decoder::new(Cursor::new(wav)) {
                    sink.append(source);
                    sink.sleep_until_end();
                }
            }
        }
    });
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_synthesize_chime_wav_header() {
        let wav = synthesize_chime(&ChimeType::FocusComplete);
        assert!(wav.len() > 44);
        assert_eq!(&wav[0..4], b"RIFF");
        assert_eq!(&wav[8..12], b"WAVE");
        assert_eq!(&wav[12..16], b"fmt ");
        assert_eq!(&wav[36..40], b"data");
    }
}
