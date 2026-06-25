import React, { useState, useCallback, useRef, useEffect } from 'react';
import { TimKiemPhim } from '../../services/FilmService';
import useRoute from '../../hooks/useRoute';

// Simple debounce utility (no lodash dependency)
function debounce(fn, delay) {
    let timer;
    const debounced = (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => fn(...args), delay);
    };
    debounced.cancel = () => clearTimeout(timer);
    return debounced;
}

export default function BookingTicketNow(props) {
    const { navigate } = useRoute();
    const { arrFilm } = props;

    const [keyword, setKeyword] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [activeIndex, setActiveIndex] = useState(-1);

    const wrapperRef = useRef(null);
    const inputRef = useRef(null);

    // Close dropdown on outside click
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
                setShowDropdown(false);
                setActiveIndex(-1);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const doSearch = useCallback(
        debounce(async (value) => {
            const term = value.trim();
            if (term === '') {
                setSearchResults([]);
                setShowDropdown(false);
                setIsLoading(false);
                return;
            }
            try {
                setIsLoading(true);
                const res = await TimKiemPhim(term);
                const results = res.data.body || [];
                setSearchResults(results);
                setShowDropdown(results.length > 0);
            } catch (error) {
                console.error('Lỗi tìm kiếm phim:', error);
                // Fallback: lọc local từ arrFilm
                const localResults = arrFilm.filter((f) =>
                    f.movieName.toLowerCase().includes(term.toLowerCase())
                );
                setSearchResults(localResults);
                setShowDropdown(localResults.length > 0);
            } finally {
                setIsLoading(false);
            }
        }, 300),
        [arrFilm]
    );

    const handleInputChange = (e) => {
        const value = e.target.value;
        setKeyword(value);
        setActiveIndex(-1);
        if (value.trim() === '') {
            setSearchResults([]);
            setShowDropdown(false);
        } else {
            doSearch(value);
        }
    };

    const handleSelectMovie = (movie) => {
        setKeyword('');
        setShowDropdown(false);
        setActiveIndex(-1);
        navigate(`detail/${movie.movieId}`);
    };

    const handleKeyDown = (e) => {
        if (!showDropdown || searchResults.length === 0) return;

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            setActiveIndex((prev) =>
                prev < searchResults.length - 1 ? prev + 1 : 0
            );
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setActiveIndex((prev) =>
                prev > 0 ? prev - 1 : searchResults.length - 1
            );
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (activeIndex >= 0 && activeIndex < searchResults.length) {
                handleSelectMovie(searchResults[activeIndex]);
            }
        } else if (e.key === 'Escape') {
            setShowDropdown(false);
            setActiveIndex(-1);
        }
    };

    const handleClear = () => {
        setKeyword('');
        setSearchResults([]);
        setShowDropdown(false);
        setActiveIndex(-1);
        inputRef.current?.focus();
    };

    const getStatusLabel = (status) => {
        switch (status) {
            case 'NOW_SHOWING':
                return { text: 'Đang chiếu', color: 'bg-emerald-500' };
            case 'COMING_SOON':
                return { text: 'Sắp chiếu', color: 'bg-amber-500' };
            default:
                return { text: status, color: 'bg-gray-500' };
        }
    };

    return (
        <div
            ref={wrapperRef}
            className="movie-search-card bg-white rounded-lg shadow-2xl py-7 px-8 w-full xl:w-3/4 mx-auto translate-y-[-50%] hidden md:block"
            style={{ position: 'relative', zIndex: 50 }}
        >
            {/* Search Input */}
            <div style={{ position: 'relative' }}>
                <div style={{
                    position: 'relative',
                    display: 'flex',
                    alignItems: 'center',
                }}>
                    {/* Search icon */}
                    <svg
                        width="18" height="18" viewBox="0 0 24 24" fill="none"
                        stroke="#94a3b8" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                        style={{ position: 'absolute', left: '14px', zIndex: 1, pointerEvents: 'none' }}
                    >
                        <circle cx="11" cy="11" r="8" />
                        <line x1="21" y1="21" x2="16.65" y2="16.65" />
                    </svg>

                    <input
                        ref={inputRef}
                        type="text"
                        value={keyword}
                        onChange={handleInputChange}
                        onKeyDown={handleKeyDown}
                        onFocus={() => {
                            if (searchResults.length > 0) setShowDropdown(true);
                        }}
                        placeholder="Nhập tên phim bạn muốn tìm..."
                        autoComplete="off"
                        style={{
                            width: '100%',
                            padding: '12px 40px 12px 44px',
                            borderRadius: '8px',
                            border: '2px solid #cbd5e1',
                            background: '#fff',
                            color: '#1e293b',
                            fontSize: '15px',
                            outline: 'none',
                            transition: 'all 0.3s ease',
                        }}
                        onMouseOver={(e) => {
                            e.target.style.borderColor = '#f97316';
                        }}
                        onMouseOut={(e) => {
                            if (document.activeElement !== e.target) {
                                e.target.style.borderColor = '#cbd5e1';
                            }
                        }}
                        onFocusCapture={(e) => {
                            e.target.style.borderColor = '#f97316';
                            e.target.style.boxShadow = '0 0 0 3px rgba(249,115,22,0.15)';
                        }}
                        onBlurCapture={(e) => {
                            e.target.style.borderColor = '#cbd5e1';
                            e.target.style.boxShadow = 'none';
                        }}
                    />

                    {/* Loading spinner or clear button */}
                    {isLoading ? (
                        <div style={{
                            position: 'absolute',
                            right: '14px',
                            width: '18px',
                            height: '18px',
                            border: '2px solid rgba(249,115,22,0.3)',
                            borderTopColor: '#f97316',
                            borderRadius: '50%',
                            animation: 'movie-search-spin 0.6s linear infinite',
                        }} />
                    ) : keyword.length > 0 ? (
                        <button
                            onClick={handleClear}
                            style={{
                                position: 'absolute',
                                right: '10px',
                                background: '#f1f5f9',
                                border: 'none',
                                borderRadius: '50%',
                                width: '24px',
                                height: '24px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                cursor: 'pointer',
                                color: '#64748b',
                                fontSize: '14px',
                                transition: 'all 0.2s',
                                padding: 0,
                            }}
                            onMouseOver={(e) => {
                                e.currentTarget.style.background = '#e2e8f0';
                                e.currentTarget.style.color = '#1e293b';
                            }}
                            onMouseOut={(e) => {
                                e.currentTarget.style.background = '#f1f5f9';
                                e.currentTarget.style.color = '#64748b';
                            }}
                        >
                            ✕
                        </button>
                    ) : null}
                </div>

                {/* Dropdown Results */}
                {showDropdown && (
                    <div style={{
                        position: 'absolute',
                        top: 'calc(100% + 8px)',
                        left: 0,
                        right: 0,
                        background: '#fff',
                        borderRadius: '12px',
                        border: '1px solid #e2e8f0',
                        boxShadow: '0 16px 48px rgba(0,0,0,0.12)',
                        maxHeight: '400px',
                        overflowY: 'auto',
                        zIndex: 100,
                        padding: '6px',
                    }}>
                        {searchResults.length === 0 && !isLoading ? (
                            <div style={{
                                padding: '24px',
                                textAlign: 'center',
                                color: '#94a3b8',
                                fontSize: '14px',
                            }}>
                                Không tìm thấy phim phù hợp
                            </div>
                        ) : (
                            searchResults.map((movie, index) => {
                                const statusInfo = getStatusLabel(movie.status);
                                const isActive = index === activeIndex;

                                return (
                                    <div
                                        key={movie.movieId || index}
                                        onClick={() => handleSelectMovie(movie)}
                                        onMouseEnter={() => setActiveIndex(index)}
                                        style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '14px',
                                            padding: '10px 12px',
                                            borderRadius: '8px',
                                            cursor: 'pointer',
                                            transition: 'all 0.2s ease',
                                            background: isActive
                                                ? '#fff7ed'
                                                : 'transparent',
                                            borderLeft: isActive
                                                ? '3px solid #f97316'
                                                : '3px solid transparent',
                                        }}
                                    >
                                        {/* Poster */}
                                        <div style={{
                                            width: '52px',
                                            height: '72px',
                                            borderRadius: '8px',
                                            overflow: 'hidden',
                                            flexShrink: 0,
                                            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                                        }}>
                                            <img
                                                src={movie.moviePoster}
                                                alt={movie.movieName}
                                                onError={(e) => {
                                                    e.target.onerror = null;
                                                    e.target.src = 'https://picsum.photos/52/72';
                                                }}
                                                style={{
                                                    width: '100%',
                                                    height: '100%',
                                                    objectFit: 'cover',
                                                }}
                                            />
                                        </div>

                                        {/* Movie Info */}
                                        <div style={{
                                            flex: 1,
                                            minWidth: 0,
                                            display: 'flex',
                                            flexDirection: 'column',
                                            gap: '4px',
                                        }}>
                                            <span style={{
                                                color: isActive ? '#1e293b' : '#475569',
                                                fontSize: '14px',
                                                fontWeight: '600',
                                                whiteSpace: 'nowrap',
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis',
                                                transition: 'color 0.2s',
                                            }}>
                                                {movie.movieName}
                                            </span>
                                            {movie.status && (
                                                <span
                                                    className={statusInfo.color}
                                                    style={{
                                                        fontSize: '11px',
                                                        fontWeight: '600',
                                                        color: '#fff',
                                                        padding: '2px 8px',
                                                        borderRadius: '4px',
                                                        width: 'fit-content',
                                                        letterSpacing: '0.3px',
                                                    }}
                                                >
                                                    {statusInfo.text}
                                                </span>
                                            )}
                                        </div>

                                        {/* Arrow */}
                                        <svg
                                            width="16" height="16" viewBox="0 0 24 24" fill="none"
                                            stroke={isActive ? '#f97316' : '#cbd5e1'}
                                            strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                                            style={{ flexShrink: 0, transition: 'all 0.2s' }}
                                        >
                                            <polyline points="9 18 15 12 9 6" />
                                        </svg>
                                    </div>
                                );
                            })
                        )}
                    </div>
                )}
            </div>

            {/* Spinner animation */}
            <style>{`
                @keyframes movie-search-spin {
                    to { transform: rotate(360deg); }
                }

                .movie-search-card input::placeholder {
                    color: #94a3b8;
                }

                .movie-search-card div::-webkit-scrollbar {
                    width: 6px;
                }
                .movie-search-card div::-webkit-scrollbar-track {
                    background: transparent;
                }
                .movie-search-card div::-webkit-scrollbar-thumb {
                    background: rgba(0,0,0,0.1);
                    border-radius: 3px;
                }
                .movie-search-card div::-webkit-scrollbar-thumb:hover {
                    background: rgba(0,0,0,0.2);
                }
            `}</style>
        </div>
    );
}
