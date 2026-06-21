import React, { useEffect, useMemo, useState } from "react";

const DEFAULT_AVATAR_VALUES = new Set([
    "default avatar",
    "default-avatar-url",
    "null",
    "undefined",
]);

export const isUsableAvatarUrl = (avatar) => {
    if (typeof avatar !== "string") return false;
    const normalized = avatar.trim();
    if (!normalized) return false;
    return !DEFAULT_AVATAR_VALUES.has(normalized.toLowerCase());
};

const pickInitialSource = ({ firstName, lastName, name, username, email }) => {
    return [firstName, lastName, name, username, email]
        .map((value) => (typeof value === "string" ? value.trim() : ""))
        .find(Boolean);
};

export const getAvatarInitial = (user = {}) => {
    const source = pickInitialSource(user);
    return source ? source.charAt(0).toLocaleUpperCase("vi-VN") : "U";
};

const getColorIndex = (value) => {
    const source = value || "user";
    let hash = 0;
    for (let i = 0; i < source.length; i += 1) {
        hash = source.charCodeAt(i) + ((hash << 5) - hash);
    }
    return Math.abs(hash);
};

const AVATAR_COLORS = [
    "#2563eb",
    "#059669",
    "#dc2626",
    "#7c3aed",
    "#ea580c",
    "#0891b2",
    "#be123c",
    "#4f46e5",
];

export default function UserAvatar({
    avatar,
    firstName,
    lastName,
    name,
    username,
    email,
    size = 48,
    className = "",
    style,
    alt,
}) {
    const [imageFailed, setImageFailed] = useState(false);

    useEffect(() => {
        setImageFailed(false);
    }, [avatar]);

    const initial = getAvatarInitial({ firstName, lastName, name, username, email });
    const colorSource = username || email || name || firstName || lastName || initial;
    const backgroundColor = useMemo(
        () => AVATAR_COLORS[getColorIndex(colorSource) % AVATAR_COLORS.length],
        [colorSource]
    );

    const hasImage = isUsableAvatarUrl(avatar) && !imageFailed;
    const dimension = typeof size === "number" ? `${size}px` : size;
    const fontSize = typeof size === "number" ? `${Math.max(16, Math.round(size * 0.45))}px` : "3rem";

    return (
        <div
            className={className}
            style={{
                width: dimension,
                height: dimension,
                minWidth: dimension,
                borderRadius: "50%",
                overflow: "hidden",
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                backgroundColor,
                color: "#fff",
                fontWeight: 700,
                fontSize,
                lineHeight: 1,
                textTransform: "uppercase",
                userSelect: "none",
                ...style,
            }}
            aria-label={alt || name || username || email || "User avatar"}
        >
            {hasImage ? (
                <img
                    src={avatar}
                    alt={alt || name || username || "User avatar"}
                    onError={() => setImageFailed(true)}
                    style={{
                        width: "100%",
                        height: "100%",
                        objectFit: "cover",
                        objectPosition: "center",
                        display: "block",
                    }}
                />
            ) : (
                <span>{initial}</span>
            )}
        </div>
    );
}
