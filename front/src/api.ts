const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

interface ApiError {
  code?: string
  message?: string
  details?: {
    champ?: string
    message: string
  }[]
}

/**
 * Fonction générique pour communiquer avec le backend.
 */
export async function apiRequest<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = localStorage.getItem('token')

  let response: Response

  try {
    response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers: {
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers
      }
    })
  } catch {
    throw new Error(
      'Impossible de contacter le serveur. Vérifiez votre connexion.'
    )
  }

  if (!response.ok) {
    let error: ApiError = {
      message: `Erreur HTTP ${response.status}`
    }

    try {
      error = await response.json() as ApiError
    } catch {
      // La réponse ne contient pas de JSON exploitable.
    }

    const message = error.details?.length
      ? `${error.message ?? 'Erreur API'} : ${error.details
          .map(detail => detail.champ
            ? `${detail.champ} : ${detail.message}`
            : detail.message)
          .join(', ')}`
      : error.message ?? `Erreur HTTP ${response.status}`

    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}
