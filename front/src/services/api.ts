
const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

interface ApiErrorResponse {
  code?: string
  message?: string
  details?: { champ?: string; message: string }[]
}

export class ApiError extends Error {
  public status: number;
  public code?: string;
  public details?: ApiErrorResponse['details'];

  constructor(
    message: string,
    status: number,
    code?: string,
    details?: ApiErrorResponse['details']
  ) {
    super(message);

    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = localStorage.getItem('token')
  const headers = new Headers(options.headers)

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  let response: Response

  try {
    response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers
    })
  } catch {
    throw new ApiError(
      'Impossible de contacter le serveur.',
      0,
      'NETWORK_ERROR'
    )
  }

  if (!response.ok) {
    let error: ApiErrorResponse = {
      message: `Erreur HTTP ${response.status}`
    }

    try {
      error = await response.json() as ApiErrorResponse
    } catch {
      // Réponse non JSON.
    }

    throw new ApiError(
      error.message ?? 'Une erreur est survenue.',
      response.status,
      error.code,
      error.details
    )
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}
